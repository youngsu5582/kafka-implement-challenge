import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

fun main(args: Array<String>) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!")

    // TODO: Uncomment the code below to pass the first stage
    var serverSocket = ServerSocket(9092)

    // Since the tester restarts your program quite often, setting SO_REUSEADDR
    // ensures that we don't run into 'Address already in use' errors
    serverSocket.reuseAddress = true

    val socket = serverSocket.accept() // Wait for connection from client.
    println("accepted new connection")

    val input = DataInputStream(socket.getInputStream())
    val output = DataOutputStream(socket.getOutputStream())

    // 4Byte
    val messageSize = input.readInt()
    // 2Byte
    val requestApiKey = input.readShort()
    // 2Byte
    val requestApiVersion = input.readShort()
    val correlationId = input.readInt()

    println(messageSize)
    println(requestApiKey)
    println(requestApiVersion)
    println(correlationId)

    output.writeInt(0)
    output.writeInt(correlationId)

    output.close()
}
