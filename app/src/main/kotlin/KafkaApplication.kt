import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.ServerSocket
import java.nio.ByteBuffer

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
                        val requestHeader = parseRequestHeader(input)
                        val request = parseRequest(requestHeader, input)
                        CustomLogger.debug("파싱결과: $request")

                        CustomLogger.debug("확인을 하기 위해 잠시 대기합니다.")
                        Thread.sleep(500)

                        val response = process(request)
                        CustomLogger.debug("처리결과: $response")

                        writeResponse(output, response)
                    } catch (e: EOFException) {
                        break
                    } catch (e: Exception) {
                        CustomLogger.debug("에러가 발생했습니다. ${e.message}")
                    }
                }
            }
            thread.start()
        }
    }

    private fun parseRequestHeader(input: DataInputStream): KafkaRequestHeader {
        // 4Byte
        val messageSize = input.readInt()
        // 2Byte
        val requestApiKey = KafkaApiKey.from(input.readShort())

        // 2Byte
        val requestApiVersion = input.readShort()

        // 4Byte
        val correlationId = input.readInt()

        val contents = parseContents(input)

        val tagBuffer = input.readByte()

        return KafkaRequestHeader(
            messageSize = messageSize,
            requestApiKey = requestApiKey,
            requestApiVersion = requestApiVersion,
            correlationId = correlationId,
            contents = contents,
            tagBuffer = tagBuffer
        )
    }

    private fun parseContents(input: DataInputStream): String {
        // 2 Byte
        val length = input.readShort()
        val byteArray = ByteArray(length.toInt())
        input.readFully(byteArray)
        return byteArray.toString(Charsets.UTF_8)
    }

    private fun parseRequest(header: KafkaRequestHeader, input: DataInputStream): KafkaRequest {
        val requestBody = parseRequestBody(header, input)
        return KafkaRequest(
            requestHeader = header,
            requestBody = requestBody
        )
    }

    private fun parseRequestBody(header: KafkaRequestHeader, input: DataInputStream): KafkaRequestBody {
        CustomLogger.debug("본문을 파싱합니다. ${header.requestApiKey}")
        return when (header.requestApiKey) {
            KafkaApiKey.API_VERSIONS -> parseApiVersionsRequestBody(input)
            KafkaApiKey.DESCRIBE_TOPIC_PARTITIONS -> parseDescribeTopicPartitionRequestBody(input)
            else -> throw NotImplementedError("Not implemented yet ${header.requestApiKey}")
        }
    }

    private fun parseDescribeTopicPartitionRequestBody(input: DataInputStream): DescribeTopicPartitionsApiRequest {
        val topicList = parseTopic(input)
        val responsePartitionLimit = input.readInt()
        val cursor = input.readByte()
        val buffer = input.readByte()
        return DescribeTopicPartitionsApiRequest(
            topics = topicList,
            responsePartitionLimit = responsePartitionLimit,
            cursor = cursor,
            tagBuffer = buffer
        )
    }

    private fun parseTopic(input: DataInputStream): List<Topic> {
        val topicLength = input.readByte().toInt()
        val list = mutableListOf<Topic>()
        (0 until topicLength - 1).forEach { _ ->
            val topic = parseCompactString(input)!!
            val buffer = input.readByte()
            list.add(
                Topic(
                    name = topic,
                    buffer = buffer,
                )
            )
        }
        return list
    }

    private fun parseApiVersionsRequestBody(input: DataInputStream): ApiVersionsRequest {
        val clientId = parseCompactString(input)
        val clientVersion = parseCompactString(input)
        return ApiVersionsRequest(
            clientId = clientId,
            clientVersion = clientVersion,
        )
    }

    private fun parseCompactString(input: DataInputStream): String? {
        val readUnsignedByte = input.readUnsignedByte()
        return when (val length = readUnsignedByte) {
            0xff -> null
            0x00 -> ""
            else -> {
                // Compact String 은 1 빼야함
                // 0x00 이 빈 문자열
                val bytes = ByteArray(length - 1)
                input.readFully(bytes)
                String(bytes, Charsets.UTF_8)
            }
        }
    }

    private fun process(request: KafkaRequest): KafkaResponse {
        val correlationId = request.correlationId()

        val responseBody = processBody(request)
        return KafkaResponse(
            responseHeader = KafkaResponseHeader(correlationId),
            responseBody = responseBody
        )
    }

    private fun processBody(request: KafkaRequest): KafkaResponseBody {
        return when (val requestApiKey = request.requestApiKey()) {
            KafkaApiKey.API_VERSIONS -> processApiVersions(request)
            KafkaApiKey.DESCRIBE_TOPIC_PARTITIONS -> processDescribeTopicPartitions(request)
            else -> throw NotImplementedError("Not implemented yet $requestApiKey")
        }
    }

    private fun processApiVersions(request: KafkaRequest): ApiVersionsResponse {
        CustomLogger.debug("ProtocolApiRequest 를 수행합니다. $request")
        val errorCode = getErrorCode(request.requestHeader)
        if (errorCode != 0.toShort()) {
            return ApiVersionsResponse(
                errorCode = errorCode,
                apiKeys = emptyList(),
                throttleTimeMs = 0,
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
        )
    }

    private fun processDescribeTopicPartitions(request: KafkaRequest): DescribeTopicPartitionsApiResponse {
        val describeTopicPartitionsApiRequest = request.bodyAs<DescribeTopicPartitionsApiRequest>()
        // 이번 Step 에선 무조건 없다고 가정
        val topics = describeTopicPartitionsApiRequest.topics.map { topic ->
            TopicResponse(
                // UNKNOWN_TOPIC_OR_PARTITION
                errorCode = 3,
                name = topic.name,
                topicId = Uuid.ZERO,
                isInternal = false,
                partitions = arrayOf(),
                topicAuthorizedOperations = TopicOperation.EMPTY
            )
        }

        return DescribeTopicPartitionsApiResponse(
            throttleTime = 0,
            topics = topics,
            nextCursor = null,
        )
    }

    fun writeResponse(output: DataOutputStream, response: KafkaResponse) {
        val headerBuffer = writeKafkaResponseHeader(response.responseHeader)
        val bodyBuffer = writeResponseBody(response.responseBody)
        val size = headerBuffer.size + bodyBuffer.size
        CustomLogger.debug("출력할 버퍼 크기: $size")
        CustomLogger.debug("출력할 본문: ${(headerBuffer + bodyBuffer).contentToString()}")
        output.writeInt(size)
        output.write(headerBuffer)
        output.write(bodyBuffer)
    }

    fun writeResponseBody(responseBody: KafkaResponseBody): ByteArray {
        return responseBody.toByteArray()
    }

    fun writeKafkaResponseHeader(header: KafkaResponseHeader): ByteArray {
        val byteArray = ByteArray(5)
        val buffer = ByteBuffer.wrap(byteArray)
        buffer.putInt(header.correlationId)
        buffer.put(header.tagBuffer)
        return byteArray
    }

    private fun getErrorCode(request: KafkaRequestHeader): Short {
        if (request.requestApiVersion in 0..4) {
            return 0
        }
        return 35
    }
}