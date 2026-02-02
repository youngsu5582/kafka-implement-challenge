enum class KafkaApiKey(
    val value: Short,
    val minVersion: Short,
    val maxVersion: Short
) {
    FETCH(1, 0, 17),
    API_VERSIONS(18, 0, 4),
    DESCRIBE_TOPIC_PARTITIONS(75, 0, 0)
}