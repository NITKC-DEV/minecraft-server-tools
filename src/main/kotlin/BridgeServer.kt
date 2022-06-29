import MSToolsProtocol.*
import okhttp3.internal.isHealthy
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedReader
import java.io.PipedWriter
import java.io.RandomAccessFile
import java.net.ServerSocket
import java.net.Socket

//CAD!CAD!CAD!CAD!
const val SOCKET_PORT = 0xCAD//3245

//橋！
class BridgeServer(
    val serverSocket: ServerSocket,
    val logFile: RandomAccessFile, val outputStream: OutputStream
) : Thread() {
    lateinit var socket: Socket
    override fun run() {
        println("Thread ${this.id}\n")
        try {
            socket = serverSocket.accept()
            var sin = socket.getInputStream()
            var sout = socket.getOutputStream()
            while (!isInterrupted) {
                if (sin.available() != 0) {
                    println("Recive")
                    val readByteArray = mutableListOf<Byte>()
                    do {
                        val r = sin.read()
                        println(r)
                        if (r != DATA_END.ordinal) {
                            readByteArray.add(r.toByte())
                        }
                    } while (r != DATA_END.ordinal)
                    val msToolsProtocol = MSToolsProtocol.values()[readByteArray.first().toInt()]
                    val result = readByteArray.drop(1).toByteArray()
                    println("PROTOCOL:$msToolsProtocol")
                    println("RESULT:${result.decodeToString()}")
                    if (msToolsProtocol == GREETING) {//( ｀・∀・´)ﾉﾖﾛｼｸ
                        sout.write(logFileRead(0))
                        sout.write(DATA_END.ordinal)
                        sout.flush()
                    } else if (msToolsProtocol == EXECUTE) {
                        val p = logFile.length()
                        outputStream.write(result)
                        outputStream.write("\n".encodeToByteArray())
                        outputStream.flush()
                        println("---Wait!!!---")
                        sleep(1000)//おやすみ
                        println("---End Waiting!!!---")
                        sout.write(logFileRead(p))
                        sout.write(DATA_END.ordinal)
                        sout.flush()
                    }
                }

                if (!socket.isConnected) {
                    println("Closed")
                    socket = serverSocket.accept()
                    sin = socket.getInputStream()
                    sout = socket.getOutputStream()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    fun logFileRead(seek: Long): ByteArray {
        var b: Byte
        val a = mutableListOf<Byte>()
        logFile.seek(seek)
        while (logFile.read().also { b = it.toByte() } != -1) {
            a.add(b)
        }
        logFile.seek(logFile.length())
        return a.toByteArray()
    }

    override fun interrupt() {
        serverSocket.close()
        super.interrupt()
    }
}