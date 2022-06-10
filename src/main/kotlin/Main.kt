import kotlinx.cli.*
import net.mm2d.log.Logger
import net.mm2d.log.Senders
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.ControlPoint.DiscoveryListener
import net.mm2d.upnp.ControlPointFactory
import net.mm2d.upnp.Device
import net.mm2d.upnp.Protocol
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
    println("Super Ultimate Max Large Big Long Ultra Powerful Useful Helpful Minecraft Server Tools Version Perfect Stable created by naotiki")
    val parser = ArgParser("mstools")

    class Debug : Subcommand("debug", "デバッグ用") {

        override fun execute() {
            println("デバッグモード")
            println("ポートぶち開けモードの検証中")

            println("ポートぶち開けるます")
            var ipv4 = try {
                InetAddress.getLocalHost().hostAddress
            } catch (e: Exception) {
                println("W T F")
                throw e
            }
            println("IPv4アドレス:$ipv4")
            print("Enterで続行 又は右にIPv4アドレスを入力:")
            ipv4 = readlnOrNull()?.ifEmpty { null } ?: ipv4
            println("IPv4アドレス $ipv4 で続行します")
            println()
            println("Upnpをぶち開けるます")
            Logger.setLogLevel(Logger.VERBOSE)
            //Logger.setSender(Senders.create())
            val cp = ControlPointFactory.create(Protocol.IP_V4_ONLY).also { cp ->
                cp.addDiscoveryListener(object : DiscoveryListener {
                    override fun onDiscover(device: Device) {
                        println(device.udn)
                        println(device.ipAddress)
                        fun getService()= device.findDeviceByTypeRecursively("urn:schemas-upnp-org:device:WANConnectionDevice:1")?.run {
                            findServiceByType("urn:schemas-upnp-org:service:WANPPPConnection:1")
                                ?: findServiceByType("urn:schemas-upnp-org:service:WANIPConnection:1")
                        }
                       /*getService()?.findAction("AddPortMapping")?.also { action ->
                            println("In")
                            action.invoke(
                                mapOf(
                                    "NewExternalPort" to "12345",
                                    "NewInternalPort" to "12345",
                                    "NewEnabled" to "1",
                                    "NewPortMappingDescription" to "*TEST* Minecraft Server Tools",
                                    "NewProtocol" to "TCP",//UDP
                                    "NewLeaseDuration" to "0",
                                    "NewInternalClient" to ipv4
                                ), onResult = {
                                    println(it)
                                }, onError = {
                                    throw it
                                }
                            )
                        } ?: kotlin.run {
                            println("No")
                        }*/
                        /*getService()?.findAction("DeletePortMapping")?.also { action ->
                            println("In")
                            action.invoke(
                                mapOf(
                                    "NewExternalPort" to "12345",
                                    "NewProtocol" to "TCP",//UDP
                                ), onResult = {
                                    println(it)
                                }, onError = {
                                    throw it
                                }
                            )
                        } ?: kotlin.run {
                            println("No")
                        }*/
                    }

                    override fun onLost(device: Device) {
                    }

                })
                cp.initialize()
                cp.start()
            }
            println("デバイスを検索中")
            cp.search("urn:schemas-upnp-org:service:WANPPPConnection:1")


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
        inner class Open : Subcommand("open", "ネットワークを開けるます") {

            override fun execute() {

            }
        }

        inner class Close : Subcommand("close", "ネットワークを閉じるます") {

            override fun execute() {

            }
        }

        override fun execute() {

        }
    }

    val sync = Sync()
    val debug = Debug()
    parser.subcommands(sync, debug)
    parser.parse(args)
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

