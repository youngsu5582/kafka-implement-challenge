data class ProtocolApiRequest(
    val messageSize: Int,
    val requestApiKey: Short,
    val requestApiVersion: Short,
    val correlationId: Int,
)

abstract class ProtocolApiResponse(
    open val correlationId: Int
)