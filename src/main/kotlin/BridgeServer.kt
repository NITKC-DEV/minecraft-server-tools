import MSToolsProtocol.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

//CAD!CAD!CAD!CAD!
const val SOCKET_PORT=0xCAD
//橋！
class BridgeServer(
    val serverSocket: ServerSocket,
    val serverProcess: Process//val inputStream: BufferedReader, val outputStream: OutputStream
) :
    Thread() {
    lateinit var socket: Socket
    override fun run() {
        serverProcess.
        println("Thread ${this.id}\n")
        try {
            socket = serverSocket.accept()
            val sin = socket.getInputStream()
            val sout = socket.getOutputStream()

            while (!isInterrupted) {
                if (sin.available()!=0){
                    println("Recive")
                    val readByteArray= mutableListOf<Byte>()
                    do{
                        val r=sin.read()
                        println(r)
                        if (r!=DATA_END.ordinal) {
                            readByteArray.add(r.toByte())
                        }
                    }while (r!=DATA_END.ordinal)

                    val msToolsProtocol=MSToolsProtocol.values()[readByteArray.first().toInt()]
                    val result= readByteArray.drop(1).toByteArray().decodeToString()
                    println("PROTOCOL:$msToolsProtocol")
                    println("RESULT:$result")
                    if (msToolsProtocol== GREETING){//( ｀・∀・´)ﾉﾖﾛｼｸ

                        val a=inputStream.readText().encodeToByteArray()
                        sout.write(a)
                        println(a)
                        sout.write(DATA_END.ordinal)
                        sout.flush()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }


    override fun interrupt() {
        serverSocket.close()
        super.interrupt()
    }
}