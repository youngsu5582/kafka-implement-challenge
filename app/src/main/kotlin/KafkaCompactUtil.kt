import java.io.DataOutputStream
import java.util.UUID

fun DataOutputStream.writeCompactString(str: String) {
    val bytes = str.toByteArray(Charsets.UTF_8)
    writeByte(bytes.size + 1)
    write(bytes)
}

fun DataOutputStream.writeCompactInt(int: Int?) {
    if (int == null) {
        writeByte(0xff)
    } else {
        writeByte(int)
    }
}

fun DataOutputStream.writeUuid(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}