object ApiVersionsConstants {
    const val API_KEY: Short = 18
    const val MIN_VERSION: Short = 0
    const val MAX_VERSION: Short = 4
}

data class ApiVersionsResponse(
    override val correlationId: Int,
    val errorCode: Short,
    val apiKeys: List<ApiVersionsApiKeysItem>,
    val throttleTimeMs: Int,
    val tagBuffer: Byte = 0
) : ProtocolApiResponse(correlationId)

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