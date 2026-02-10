data class ApiVersionsRequest(
    val clientId: ClientId,
    val clientVersion: ClientVersion,
    val tagBuffer: Byte = 0
)

data class ClientId(
    val length: Int,
    val contents: String
)

data class ClientVersion(
    val length: Int,
    val contents: String
)

data class ApiVersionsResponse(
    override val correlationId: Int,
    val errorCode: Short,
    val apiKeys: List<ApiVersionsApiKeysItem>,
    val throttleTimeMs: Int,
    val tagBuffer: Byte = 0
) : KafkaResponseHeader(correlationId)

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