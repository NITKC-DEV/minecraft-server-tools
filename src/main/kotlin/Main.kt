import NetworkMode.*
import kotlinx.cli.*
import net.mm2d.log.Logger
import net.mm2d.upnp.ControlPoint.DiscoveryListener
import net.mm2d.upnp.ControlPointFactory
import net.mm2d.upnp.Device
import net.mm2d.upnp.Protocol
import net.mm2d.upnp.Service
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.sshd.SshdSessionFactory
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@ExperimentalCli
fun main(args: Array<String>) {
    //ぼくのかんがえたさいきょうのなまえ
    println("Super Ultimate Max Large Big Long Ultra Powerful Useful Helpful Minecraft Server Tools Version Perfect Stable Created by @naotikikt")
    val parser = ArgParser("mstools")

    class Debug : Subcommand("debug", "デバッグ用") {
        override fun execute() {

        }

    }

    class Sync : Subcommand("sync", "ワールドを同期するます") {
        val repoPath by option(ArgType.String, "repo", "r", "リポジトリの同期先を指定するます").default(Paths.get("").toString())
        val sshKeyPath by option(ArgType.String, "key-path", description = "SSH鍵のパスを指定します。")
        var result: Int = 0

        override fun execute() {
            val builder = FileRepositoryBuilder()
            val repository: Repository =
                builder.setGitDir(File(repoPath + Constants.DOT_GIT)).readEnvironment().findGitDir().build()

            val git = Git(repository)

            git.push().setTransportConfigCallback {
                if (it is SshTransport) {
                    it.sshSessionFactory = sshSessionFactory
                }
            }.call()
        }
    }

    class Network : Subcommand("network", "ネットワークの設定をするます") {

        val mode by argument(ArgType.Choice<NetworkMode>(), "モード")



        var ipv4 by option(ArgType.String, "ipv4", "i", "ぶち開けるipv4アドレスを指定します")
        override fun execute() {
            when {
                (mode == Open) || (mode == Close) -> {
                    val supportedDevices = mutableListOf<Service>()
                    val cp = ControlPointFactory.create(Protocol.IP_V4_ONLY).also { cp ->
                        cp.addDiscoveryListener(object : DiscoveryListener {
                            fun getService(device: Device) = device.findDeviceByTypeRecursively(
                                "urn:schemas-upnp-org:device:WANConnectionDevice:1"
                            )?.run {
                                findServiceByType("urn:schemas-upnp-org:service:WANPPPConnection:1")
                                    ?: findServiceByType("urn:schemas-upnp-org:service:WANIPConnection:1")
                            }

                            override fun onDiscover(device: Device) {

                                getService(device)?.let {
                                    if (it.findAction("AddPortMapping") != null &&
                                        it.findAction("DeletePortMapping") != null
                                    ) {
                                        //NATいけるデバイス
                                        println("発見:${device.udn}")
                                        supportedDevices.add(it)
                                    }
                                }
                            }

                            override fun onLost(device: Device) {
                                println("切断:${device.udn}")
                                supportedDevices.remove(getService(device))
                            }

                        })
                        cp.initialize()
                        cp.start()
                    }

                    fun getPorts(): Pair<List<Int>, List<Int>>? {
                        println("TCPのポート番号を指定してください (Enterで開けずに続行)")
                        print("複数開ける場合は\",\"で区切ってください (例:80,8080) :")
                        val t = readlnOrNull()?.split(",")?.map { it.toIntOrNull() }?.filterNotNull().orEmpty()
                        if (t.isEmpty()) {
                            println("TCPポートを設定しません")
                        } else {
                            println("次のポートが設定されます =>${t.joinToString(",")}")
                        }

                        print("UDPのポート番号を指定してください (Enterで開けずに続行):")
                        val u = readlnOrNull()?.split(",")?.map { it.toIntOrNull() }?.filterNotNull().orEmpty()
                        if (u.isEmpty()) {
                            println("UDPポートを設定しません")
                        } else {
                            println("次のポートが設定されます =>${u.joinToString(",")}")
                        }
                        if (t.isEmpty() && u.isEmpty()) {
                            println("何しに来たの？？？？？？？？帰れ")//辛辣
                            return null
                        }
                        return t to u
                    }
                    when (mode) {
                        Open -> {
                            ipv4 = ipv4 ?: try {
                                InetAddress.getLocalHost().hostAddress
                            } catch (e: Exception) {
                                println("What The Fuck !?!?")
                                throw e
                            }
                            println("IPv4アドレス:$ipv4")
                            print("Enterで続行 変更する場合は右にIPv4アドレスを入力:")
                            ipv4 = readlnOrNull()?.ifEmpty { null } ?: ipv4
                            println("IPv4アドレス $ipv4 で続行します")
                            val (TCPPorts, UDPPorts) = getPorts() ?: run { return }

                            println("UPnPのNAT設定してぶち開けるます")
                            Logger.setLogLevel(Logger.VERBOSE)
                            //Logger.setSender(Senders.create())

                            println("対応デバイスを検索中")
                            cp.search("urn:schemas-upnp-org:service:WANPPPConnection:1")
                            while (supportedDevices.isEmpty()) {
                                print("")
                            }
                            println("対応デバイスが見つかりました")
                            println("デバイス:${supportedDevices.first().device.ipAddress}のNATを設定します")
                            supportedDevices.first().findAction("AddPortMapping")!!.also { action ->
                                println("Action:AddPortMapping")
                                TCPPorts.forEach {
                                    println("TCP Open:$it")
                                    println(
                                        action.invokeSync(
                                            mapOf(
                                                "NewExternalPort" to it.toString(),
                                                "NewInternalPort" to it.toString(),
                                                "NewEnabled" to "1",
                                                "NewPortMappingDescription" to "*TEST* Minecraft Server Tools",
                                                "NewProtocol" to "TCP",//UDP
                                                "NewLeaseDuration" to "0",//seconds
                                                "NewInternalClient" to ipv4
                                            )
                                        )
                                    )
                                }
                                UDPPorts.forEach {
                                    println("UDP Open:$it")
                                    println(
                                        action.invokeSync(
                                            mapOf(
                                                "NewExternalPort" to it.toString(),
                                                "NewInternalPort" to it.toString(),
                                                "NewEnabled" to "1",
                                                "NewPortMappingDescription" to "*TEST* Minecraft Server Tools",
                                                "NewProtocol" to "UDP",//UDP
                                                "NewLeaseDuration" to "0",//seconds
                                                "NewInternalClient" to ipv4
                                            )
                                        )
                                    )
                                }

                            }
                        }
                        Close -> {
                            println("閉じます")
                            val (TCPPorts, UDPPorts) = getPorts() ?: run { return }
                            println("対応デバイスを検索中")
                            cp.search("urn:schemas-upnp-org:service:WANPPPConnection:1")
                            while (supportedDevices.isEmpty()) {
                                print("")
                            }
                            println("対応デバイスが見つかりました")
                            println("デバイス:${supportedDevices.first().device.ipAddress}のNATを設定します")
                            supportedDevices.first().findAction("DeletePortMapping")!!.also { action ->
                                println("Action:DeletePortMapping")
                                TCPPorts.forEach {
                                    println("TCP Close:$it")
                                    println(
                                        action.invokeSync(
                                            mapOf(
                                                "NewExternalPort" to it.toString(),
                                                "NewProtocol" to "TCP",//UDP
                                            )
                                        )
                                    )
                                }
                                UDPPorts.forEach {
                                    println("UDP Close:$it")
                                    println(
                                        action.invokeSync(
                                            mapOf(
                                                "NewExternalPort" to it.toString(),
                                                "NewProtocol" to "UDP",//UDP
                                            )
                                        )
                                    )
                                }

                            }
                        }
                        else -> {}
                    }


                }
                mode == DDNS -> TODO()
                mode == Check -> TODO()
            }
            return
        }
    }

    val sync = Sync()
    val debug = Debug()
    val network = Network()
    parser.subcommands(sync, debug, network)
    parser.parse(args)
    return
}

var sshSessionFactory: SshSessionFactory =
    object : SshdSessionFactory() {
        val SSH_DIR: Path = Path.of("")/* TODO(Super Max SSH Path) */

        private var privateKeyFile: Path? = null
        private var sshKey: ByteArray = byteArrayOf()

        fun CustomSshSessionFactory(sshKey: ByteArray) {
            this.sshKey = sshKey
            privateKeyFile = Path.of("mstools", SSH_DIR.toString())
        }

        override fun getSshDirectory(): File? {
            return try {
                Files.createDirectories(SSH_DIR).toFile()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        override fun getDefaultIdentities(sshDir: File?): List<Path?>? {
            try {
                Files.write(privateKeyFile, sshKey)
                return listOf(privateKeyFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return emptyList<Path>()
        }
    }

enum class NetworkMode {
    Open,
    Close,
    DDNS,
    Check,
}