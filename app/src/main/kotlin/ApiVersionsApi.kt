import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class ApiVersionsRequest(
    val clientId: String?,
    val clientVersion: String?,
    val tagBuffer: Byte = 0
) : KafkaRequestBody

data class ApiVersionsResponse(
    val errorCode: Short,
    val apiKeys: List<ApiVersionsApiKeysItem>,
    val throttleTimeMs: Int,
    val tagBuffer: Byte = 0
) : KafkaResponseBody {
    override fun toByteArray(): ByteArray {
        CustomLogger.debug("ApiVersionResponse 를 Byte 로 변환합니다. $this")
        val buffer = ByteArrayOutputStream()

        DataOutputStream(buffer).apply {
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
}

data class ApiVersionsApiKeysItem(
    val apiKey: Short,
    val minVersion: Short,
    val maxVersion: Short,
    val tagBuffer: Byte = 0
) {
    companion object {
        fun from(apiKey: KafkaApiKey): ApiVersionsApiKeysItem = ApiVersionsApiKeysItem(
            apiKey = apiKey.value,
            minVersion = apiKey.minVersion,
            maxVersion = apiKey.maxVersion
        )
    }
}