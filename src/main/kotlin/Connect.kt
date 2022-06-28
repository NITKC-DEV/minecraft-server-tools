import MSToolsProtocol.DATA_END
import MSToolsProtocol.GREETING
import kotlinx.cli.*
import java.net.Socket


//const val GREETING="MINECRAFT_SERVER_TOOLS_HANDSHAKE"
//最初のデータ1Byteは以下の列挙型を使用して通信種別を表す。
enum class MSToolsProtocol {
    DATA_END,

    GREETING,
    EXECUTE,
}

@OptIn(ExperimentalCli::class)
//え？コネクト？気持ち良すぎだろ！
class Connect : Subcommand("connect", "Minecraft ServerにSocket通信します") {
    private val server by option(ArgType.String,"server","s").default("127.0.0.1")
    private val commandArgs by argument(ArgType.String, "command").vararg().optional()

    //private val oneShot by option(ArgType.Boolean,"once","o","一発限りのコマンドを実行します").default(false)
    override fun execute() {
        val command = commandArgs.joinToString(" ")
        val socket = Socket(server, SOCKET_PORT)
        val inputStream = socket.getInputStream()
        val outputStream = socket.getOutputStream()
        //はじめましてしましょうねー
        outputStream.write(GREETING.ordinal)
        outputStream.write(DATA_END.ordinal)
        outputStream.flush()
        val readByteArray = mutableListOf<Byte>()
        do {
            val i = inputStream.read()
            if (i != DATA_END.ordinal) {
                readByteArray.add(i.toByte())
            }
        } while (i != DATA_END.ordinal)
        println(readByteArray.toByteArray().decodeToString())


    }
}