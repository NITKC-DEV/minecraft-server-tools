import kotlinx.cli.*
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.zip.ZipInputStream

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
        override fun execute() {
            val p=ProcessBuilder()
            p.environment()
            p.inheritIO()
            p.start()
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
                    val gitLocalRepo = prompt("同期するディレクトリの（絶対・相対）パスを入力(設定しない場合はそのままEnter):")
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

                    Config(
                        gitLocalRepo,
                        gitRemoteRepo,
                        sshKeyPath,
                        ddnsName,
                        ddnsPassword,
                        ipAddress,
                        tcp,
                        udp
                    )

                } else Config()
                outFile.write(json.encodeToString(Config.serializer(), conf).encodeToByteArray())
                outFile.close()
            }
            println("設定ファイルの詳しい書き方は https://github.com/NITKC22s/minecraft-server-tools/docs/ConfigFile.md を見てください")
        }

    }


    val sync = Sync(config)
    val debug = Debug()
    val network = Network(config)
    val init = Initialize()
    parser.subcommands(sync, debug, network, init)
    parser.parse(args)
    return
}


