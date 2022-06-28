import kotlinx.cli.*
import kotlinx.serialization.json.Json
import okhttp3.internal.closeQuietly
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Console
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedReader
import java.lang.System.`in`
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString


//じぇーそんのフォーマット
private val json = Json {
    prettyPrint = true
    encodeDefaults = true
}

fun prompt(question: String): String? {
    print(question)
    return readlnOrNull()?.ifEmpty { null }
}

@ExperimentalCli
fun main(args: Array<String>) {
    //ぼくのかんがえたさいきょうのなまえ
    println(
        "Super Turbo Intelligent Ultimate Dynamite Max Large Big Long Ultra Powerful Useful Helpful Better Best Good God Delicious Beautiful Most Much Many Strong String Extend Infinity Legend Plus Power Fire Final Protected Defender Minecraft Server Tools Version Really Perfect Stable Long Term Support Professional Enterprise Edition"
    )
    val parser = ArgParser("mstools")
    val isInitialized = configFile.exists()
    val config = if (isInitialized) Config.readConfig() else null

    class Debug : Subcommand("debug", "デバッグ用") {
        val serverFile by argument(ArgType.String, "ServerFile", "マイクラサーバーのJarファイルを指定しましょう").optional()
            .default(config?.serverJar.orEmpty())

        override fun execute() {
            if (serverFile.isEmpty()) {
                throw IllegalArgumentException("サーバーファイルを指定しましょう")
            }
            val path = Path(serverFile).absolutePathString()
            val p = ProcessBuilder("java", "-jar", path, "-nogui")
            println("Run: ${p.command().joinToString(" ")}")
            p.environment()
            p.redirectInput(ProcessBuilder.Redirect.PIPE)
            p.redirectOutput(ProcessBuilder.Redirect.PIPE)


            val proccess = p.start()

            /*TODO inputStreamを奪い合いしてる
            * TODO inputStreamをコピーする？もしくは２つそれぞれつくる
            * TODO BridgeServerにすべてバイパスさせる。---Agree!
            * */
            val t = BridgeServer(ServerSocket(SOCKET_PORT).apply {
                reuseAddress = true
            }, proccess)//proccess.inputStream,proccess.outputStream)
            t.start()


            val bufferRead = proccess.inputStream.bufferedReader()

            val a = object : Thread() {
                val consoleRead = `in`.buffered()
                val out = proccess.outputStream.bufferedWriter()
                override fun run() {
                    println("Started")
                    while (!isInterrupted) {
                        if (consoleRead.available() > 0) {
                            val bytes = mutableListOf<Byte>()
                            var byte: Byte = 0
                            while (!bytes.toByteArray().decodeToString()
                                    .contains("\r\n|[\n\r\u2028\u2029\u0085]".toRegex()) && consoleRead.read()
                                    .also { byte = it.toByte() } != -1
                            ) {
                                bytes.add(byte)
                            }
                            val line = bytes.toByteArray().decodeToString()
                            out.write(line)
                            out.flush()
                        }
                    }
                }

                override fun interrupt() {
                    //consoleRead.close()
                    out.close()

                    super.interrupt()

                }
            }

            a.start()
            proccess.onExit().thenApply {
                println("Clean UP")
                a.interrupt()
                t.interrupt()
                bufferRead.close()
                println("END")
            }



            var line: String? = null
            while (bufferRead.readLine().also { line = it } != null) {
                println(line)
            }


            //  proccess.waitFor()

            // ProcessHandle.of(0).stream()

        }
    }

    class Initialize : Subcommand("init", "設定ファイルをプロンプト形式で生成して初期化します") {
        val default by option(ArgType.Boolean, "default", "d", "質問なしで設定ファイルを生成します").default(false)
        override fun execute() {
            if (isInitialized) {
                println(
                    """
                    すでに初期化されています。
                    ${configFile.absolutePath}を削除するか直接中身を書き換えてください
                """.trimIndent()
                )
            } else {
                println(
                    """
                    設定ファイルを生成します
                    ${configFile.absolutePath}が生成されます
                """.trimIndent()
                )
                val outFile = configFile.outputStream()
                val conf = if (!default) {
                    //Git設定
                    val serverDirectory = prompt("同期するディレクトリの（絶対・相対）パスを入力(設定しない場合はそのままEnter):") ?: "./"
                    val gitRemoteRepo = prompt("GitリポジトリURLを入力 (設定しない場合はそのままEnter):")
                    val sshKeyPath = if (gitRemoteRepo != null) {
                        prompt("管理者から渡されたSSHキーを置いた（絶対・相対）パスを入力 (設定しない場合はそのままEnter):")
                    } else null
                    //DDNS now設定
                    val ddnsName = prompt("DDNS nowユーザー名を入力 (設定しない場合はそのままEnter):")
                    val ddnsPassword = if (ddnsName != null) {
                        prompt("DDNS nowパスワード(APIトークン)を入力 (設定しない場合はそのままEnter):")
                    } else null
                    val ipAddress = prompt("サーバーのIPv4アドレスを入力（デフォルトでは自動で設定されます）:") ?: AUTO_IP
                    println("TCPのポート番号を指定してください (Enterで変更せずに続行)")
                    print("複数開ける場合は\",\"で区切ってください (例:80,8080) :")
                    val tcp = readlnOrNull()?.split(",")?.map { it.toIntOrNull() }?.filterNotNull().orEmpty()
                    if (tcp.isEmpty()) {
                        println("TCPポートを設定しません")
                    } else {
                        println("次のポートが設定されます =>${tcp.joinToString(",")}")
                    }

                    print("UDPのポート番号を指定してください (Enterで変更せずに続行):")
                    val udp = readlnOrNull()?.split(",")?.map { it.toIntOrNull() }?.filterNotNull().orEmpty()
                    if (udp.isEmpty()) {
                        println("UDPポートを設定しません")
                    } else {
                        println("次のポートが設定されます =>${udp.joinToString(",")}")
                    }
                    if (tcp.isEmpty() && udp.isEmpty()) {
                        println("なにも設定されせん")
                    }

                    val jarFile = prompt("マイクラサーバーのJARのパスを入力:")
                    val jvmArgs = prompt("サーバー起動時のJVM引数を入力「,」区切り(デフォルトでは$DEFAULT_ARGS):")?.split(",") ?: DEFAULT_ARGS

                    Config(
                        serverDirectory,
                        gitRemoteRepo,
                        sshKeyPath,
                        ddnsName,
                        ddnsPassword,
                        ipAddress,
                        tcp,
                        udp,
                        jarFile,
                        jvmArgs,
                    )

                } else Config()
                outFile.write(json.encodeToString(Config.serializer(), conf).encodeToByteArray())
                outFile.close()
            }
            println("設定ファイルの詳しい書き方は https://github.com/NITKC22s/minecraft-server-tools/tree/master/docs/ConfigFile.md を見てください")
        }

    }


    val sync = Sync(config)
    val debug = Debug()
    val network = Network(config)
    val init = Initialize()
    val connect = Connect()
    parser.subcommands(sync, debug, network, init, connect)
    parser.parse(args)
    return
}


