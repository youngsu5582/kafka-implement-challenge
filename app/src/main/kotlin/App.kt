const val PORT = 9092

fun main(args: Array<String>) {
    CustomLogger.setLevel(LogLevel.DEBUG)
    val application = KafkaApplication()
    application.start()
}
