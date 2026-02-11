data class KafkaRequestHeader(
    val messageSize: Int,
    val requestApiKey: KafkaApiKey,
    val requestApiVersion: Short,
    val correlationId: Int,
    val contents: String,
    val tagBuffer: Byte
)

data class KafkaResponseHeader(
    val correlationId: Int,
    val tagBuffer: Byte = 0,
)

data class KafkaRequest(
    val requestHeader: KafkaRequestHeader,
    val requestBody: KafkaRequestBody
) {
    fun correlationId() = requestHeader.correlationId
    fun requestApiKey() = requestHeader.requestApiKey
    inline fun <reified T : KafkaRequestBody> bodyAs(): T {
        return requestBody as T
    }
}

data class KafkaResponse(
    val responseHeader: KafkaResponseHeader,
    val responseBody: KafkaResponseBody
)