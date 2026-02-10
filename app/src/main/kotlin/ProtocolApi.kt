data class KafkaRequestHeader(
    val messageSize: Int,
    val requestApiKey: Short,
    val requestApiVersion: Short,
    val correlationId: Int,
    val contents: String
)

abstract class KafkaResponseHeader(
    open val correlationId: Int
)