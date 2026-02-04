import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.ServerSocket

class KafkaApplication {
    fun start() {
        val serverSocket = ServerSocket(PORT)

        // Since the tester restarts your program quite often, setting SO_REUSEADDR
        // ensures that we don't run into 'Address already in use' errors
        serverSocket.reuseAddress = true

        CustomLogger.debug("서버 소켓이 구동되었습니다. 포트: $PORT")

        while (true) {
            val socket = serverSocket.accept() // Wait for connection from client.
            println("accepted new connection")
            val thread = Thread {
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())
                while (socket.isConnected) {
                    try {
                        val protocolApiRequest = parseProtocolRequest(input)
                        val apiVersionRequest = parseApiVersionRequest(input)
                        CustomLogger.debug("확인을 하기 위해 잠시 대기합니다.")
                        Thread.sleep(500)

                        val response = processApiVersions(protocolApiRequest)
                        writeApiVersionsResponse(output, response)
                    } catch (e: EOFException) {
                        break
                    }
                }
            }
            thread.start()
        }
    }

    private fun parseProtocolRequest(input: DataInputStream): ProtocolApiRequest {
        // 4Byte
        val messageSize = input.readInt()
        // 2Byte
        val requestApiKey = input.readShort()

        // 2Byte
        val requestApiVersion = input.readShort()

        // 4Byte
        val correlationId = input.readInt()

        return ProtocolApiRequest(
            messageSize = messageSize,
            requestApiKey = requestApiKey,
            requestApiVersion = requestApiVersion,
            correlationId = correlationId,
        )
    }

    private fun parseApiVersionRequest(input: DataInputStream): ApiVersionsRequest {
        val clientId = parseClientId(input)
        val clientVersion = parseClientVersion(input)
        return ApiVersionsRequest(
            clientId = clientId,
            clientVersion = clientVersion,
        )
    }

    private fun parseClientId(input: DataInputStream): ClientId {
        val length = input.readShort().toInt()
        val bytes = ByteArray(length)
        input.readFully(bytes)

        return ClientId(
            length = length,
            contents = String(bytes, Charsets.UTF_8),
        )
    }

    private fun parseClientVersion(input: DataInputStream): ClientVersion {
        val length = input.readShort().toInt()
        val bytes = ByteArray(length)
        input.readFully(bytes)

        return ClientVersion(
            length = length,
            contents = String(bytes, Charsets.UTF_8),
        )
    }

    private fun processApiVersions(request: ProtocolApiRequest): ApiVersionsResponse {
        CustomLogger.debug("ProtocolApiRequest 를 수행합니다. $request")

        val correlationId = request.correlationId
        val errorCode = getErrorCode(request)
        if (errorCode != 0.toShort()) {
            return ApiVersionsResponse(
                errorCode = errorCode,
                apiKeys = emptyList(),
                throttleTimeMs = 0,
                correlationId = correlationId,
            )
        }
        return ApiVersionsResponse(
            errorCode = errorCode,
            apiKeys = listOf(
                ApiVersionsApiKeysItem.from(KafkaApiKey.FETCH),
                ApiVersionsApiKeysItem.from(KafkaApiKey.API_VERSIONS),
                ApiVersionsApiKeysItem.from(KafkaApiKey.DESCRIBE_TOPIC_PARTITIONS),
            ),
            throttleTimeMs = 0,
            correlationId = correlationId
        )
    }

    private fun writeApiVersionsResponse(output: DataOutputStream, apiVersionsResponse: ApiVersionsResponse) {
        val byteArray = apiVersionsResponse.toByteArray()
        output.writeInt(byteArray.size)
        output.write(byteArray)
    }

    fun ApiVersionsResponse.toByteArray(): ByteArray {
        CustomLogger.debug("ApiVersionResponse 를 Byte 로 변환합니다. $this")
        val buffer = ByteArrayOutputStream()

        DataOutputStream(buffer).apply {
            writeInt(correlationId)
            writeShort(errorCode.toInt())

            // 0 는 null, 1 은 빈 배열을 의미
            writeByte(apiKeys.size + 1)
            apiKeys.forEach {
                writeShort(it.apiKey.toInt())
                writeShort(it.minVersion.toInt())
                writeShort(it.maxVersion.toInt())
                writeByte(it.tagBuffer.toInt())
            }
            writeInt(throttleTimeMs)
            writeByte(tagBuffer.toInt())
        }

        CustomLogger.debug(
            "ApiVersionResponse Byte 변환 결과. 크기: ${buffer.size()} 결과: \n" +
                    buffer.toByteArray().contentToString()
        )

        return buffer.toByteArray()
    }

    private fun getErrorCode(request: ProtocolApiRequest): Short {
        if (request.requestApiVersion in 0..4) {
            return 0
        }
        return 35
    }
}