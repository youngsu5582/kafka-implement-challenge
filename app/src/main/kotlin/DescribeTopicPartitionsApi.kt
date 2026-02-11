import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.UUID

data class DescribeTopicPartitionsApiRequest(
    val topics: List<Topic>,
    val responsePartitionLimit: Int,
    val cursor: Byte?,
    val tagBuffer: Byte = 0
) : KafkaRequestBody

data class Topic(
    val name: String,
    val buffer: Byte = 0,
)

data class DescribeTopicPartitionsApiResponse(
    val throttleTime: Int,
    val topics: List<TopicResponse>,
    val nextCursor: Int?,
    val tagBuffer: Byte = 0
) : KafkaResponseBody {
    override fun toByteArray(): ByteArray {
        CustomLogger.debug("DescribeTopicPartitionsApiResponse 를 Byte 로 변환합니다. $this")
        val buffer = ByteArrayOutputStream()

        DataOutputStream(buffer).apply {
            writeInt(throttleTime)

            // 0 는 null, 1 은 빈 배열을 의미
            writeByte(topics.size + 1)
            topics.forEach {
                writeShort(it.errorCode)
                writeCompactString(it.name)
                writeUuid(it.topicId)
                writeBoolean(it.isInternal)
                writeByte(it.partitions.size + 1)
                writeInt(it.topicAuthorizedOperations)
                writeByte(it.tagBuffer.toInt())
            }

            writeCompactInt(nextCursor)
            writeByte(tagBuffer.toInt())
        }

        CustomLogger.debug(
            "DescribeTopicPartitionsApiResponse Byte 변환 결과. 크기: ${buffer.size()} 결과: \n" +
                    buffer.toByteArray().contentToString()
        )

        return buffer.toByteArray()
    }
}

data class TopicResponse(
    val errorCode: Int,
    val name: String,
    val topicId: UUID,
    val isInternal: Boolean,
    // 지금은 비어있는 배열이라서 알수 없으므로 Array<Byte>
    val partitions: Array<Byte>,
    val topicAuthorizedOperations: Int,
    val tagBuffer: Byte = 0
)

object TopicOperation {
    const val EMPTY = 0
    const val READ = 1 shl 3           // 0x0008, bit index 3
    const val WRITE = 1 shl 4          // 0x0010, bit index 4
    const val CREATE = 1 shl 5         // 0x0020, bit index 5
    const val DELETE = 1 shl 6         // 0x0040, bit index 6
    const val ALTER = 1 shl 7          // 0x0080, bit index 7
    const val DESCRIBE = 1 shl 8       // 0x0100, bit index 8
    const val DESCRIBE_CONFIGS = 1 shl 10  // 0x0400, bit index 10
    const val ALTER_CONFIGS = 1 shl 11     // 0x0800, bit index 11

    // 예시: 0x0df8
    // = READ(8) + WRITE(16) + CREATE(32) + DELETE(64) +
    //   ALTER(128) + DESCRIBE(256) + DESCRIBE_CONFIGS(1024) + ALTER_CONFIGS(2048)
    // = 3576
}