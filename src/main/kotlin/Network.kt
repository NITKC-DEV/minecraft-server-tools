import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import net.mm2d.log.Logger
import net.mm2d.upnp.*
import java.net.InetAddress
import NetworkMode.*
import kotlinx.cli.ExperimentalCli

enum class NetworkMode {
    Open,
    Close,
    DDNS,
    Check,
}
fun getPorts(): Pair<List<Int>, List<Int>>? {
    println("TCPのポート番号を指定してください (Enterで変更せずに続行)")
    print("複数開ける場合は\",\"で区切ってください (例:80,8080) :")
    val t = readlnOrNull()?.split(",")?.map { it.toIntOrNull() }?.filterNotNull().orEmpty()
    if (t.isEmpty()) {
        println("TCPポートを設定しません")
    } else {
        println("次のポートが設定されます =>${t.joinToString(",")}")
    }

    print("UDPのポート番号を指定してください (Enterで変更せずに続行):")
    val u = readlnOrNull()?.split(",")?.map { it.toIntOrNull() }?.filterNotNull().orEmpty()
    if (u.isEmpty()) {
        println("UDPポートを設定しません")
    } else {
        println("次のポートが設定されます =>${u.joinToString(",")}")
    }
    if (t.isEmpty() && u.isEmpty()) {
        println("なにも設定されせん")
        return null
    }
    return t to u
}
@ExperimentalCli
class Network : Subcommand("network", "ネットワークの設定をするます") {

    val mode by argument(ArgType.Choice<NetworkMode>(), "mode")


    var ipv4 by option(ArgType.String, "ipv4", "i", "ぶち開けるipv4アドレスを指定します")

    override fun execute() {
        when {
            (mode == Open) || (mode == Close) -> {
                val supportedDevices = mutableListOf<Service>()
                val cp = ControlPointFactory.create(Protocol.IP_V4_ONLY).also { cp ->
                    cp.addDiscoveryListener(object : ControlPoint.DiscoveryListener {
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
                        val (TCPPorts, UDPPorts) = getPorts() ?: run {
                            cp.stop()
                            cp.terminate()
                            return
                        }

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
                        val (TCPPorts, UDPPorts) = getPorts() ?: run {
                            cp.stop()
                            cp.terminate()
                            return
                        }
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
                println("Clean Up")
                cp.stop()
                cp.terminate()
            }
            mode == DDNS -> {

            }
            mode == Check -> {
                println("ネットワーク環境のチェックをします")
            }
        }
        return
    }
}
