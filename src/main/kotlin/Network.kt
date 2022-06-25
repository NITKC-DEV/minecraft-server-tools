import NetworkMode.*
import kotlinx.cli.*
import net.mm2d.log.Logger
import net.mm2d.upnp.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.net.InetAddress

enum class NetworkMode {
    Open,
    Close,
    DDNS,
    Check,
}


@ExperimentalCli
class Network(val config: Config?) : Subcommand("network", "ネットワークの設定をするこまーんど") {

    val mode by argument(ArgType.Choice<NetworkMode>(), "mode", "")


    var ipv4 by option(ArgType.String, "ipv4", "i", "ぶち開けるipv4アドレスを指定します").default(config?.ipAddress.orEmpty())

    var tcpPorts by option(ArgType.Int, "tcp", "t", "ぶち開けるTCPアドレスを指定 (','区切り)").delimiter(",")
    var udpPorts by option(ArgType.Int, "udp", "u", "ぶち開けるUDPアドレスを指定 (','区切り)").delimiter(",")


    var ddnsNowUser by option(ArgType.String, "dnuser", "n", "DDNS now ユーザー名").default(config?.ddnsNowUser.orEmpty())
    var ddnsNowPassword by option(
        ArgType.String,
        "dnpass",
        "p",
        "DDNS now パスワード"
    ).default(config?.ddnsNowPassword.orEmpty())

    override fun execute() {
        when {
            (mode == Open) || (mode == Close) || (mode == Check) -> {
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
                        ipv4 = if (ipv4 == AUTO_IP) {
                            try {
                                InetAddress.getLocalHost().hostAddress
                            } catch (e: Exception) {
                                println("What The Fuck !?!?")
                                throw e
                            }
                        } else ipv4

                        if (ipv4.isBlank()) {

                            cp.stop()
                            cp.terminate()
                            throw IllegalArgumentException("IPアドレスが不正です")
                        }
                        println("IPv4アドレス $ipv4 で続行します")
                        if (tcpPorts.isEmpty() && udpPorts.isEmpty()) {
                            tcpPorts=config?.tcpPorts.orEmpty()
                            udpPorts=config?.udpPorts.orEmpty()
                            if (tcpPorts.isEmpty()&&udpPorts.isEmpty()){
                                cp.stop()
                                cp.terminate()
                                throw IllegalArgumentException("ポート指定しやがれ")
                            }
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
                            try {
                                tcpPorts.forEach {
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
                                udpPorts.forEach {
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
                            } catch (e: IOException) {
                                println("""
                                    ポートがすでに別のIPで開けられている可能性があります。
                                    mstools network closeを試して見ましょう
                                """.trimIndent())
                                throw e;
                            } catch (e:Exception){
                                throw e
                            }

                        }
                    }
                    Close -> {
                        println("閉じます")
                        if (tcpPorts.isEmpty() && udpPorts.isEmpty()) {
                            tcpPorts=config?.tcpPorts.orEmpty()
                            udpPorts=config?.udpPorts.orEmpty()
                            if (tcpPorts.isEmpty()&&udpPorts.isEmpty()){
                                cp.stop()
                                cp.terminate()
                                throw IllegalArgumentException("ポート指定しやがれ")
                            }
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
                            tcpPorts.forEach {
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
                            udpPorts.forEach {
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
                    Check -> {
                        println("ネットワーク環境のチェックをします")
                        cp.search("urn:schemas-upnp-org:service:WANPPPConnection:1")
                        while (supportedDevices.isEmpty()) {
                            print("")
                        }
                        val device = supportedDevices.first().device
                        println("対応デバイスが見つかりました")
                        device.apply {
                            println(
                                """
                            デバイス情報
                            IP:$ipAddress
                            UDN:$udn
                            URL:$baseUrl
                            UPC:$upc
                            Type:$deviceType
                            Location:$location
                            Services:${serviceList.map { it.serviceType }}
                            ModelName:$modelName
                            Desc:$description
                        """.trimIndent()
                            )
                        }

                        println("デバイス:${device.ipAddress}に設定されます")
                    }
                    else->{}
                }
                println("Clean Up")
                cp.stop()
                cp.terminate()
            }
            mode == DDNS -> {//Dynamicで動的でDNSなドメインネームサーバー
                println("D!D!N!S!")
                if (ddnsNowUser.isEmpty() || ddnsNowPassword.isEmpty())
                    throw IllegalArgumentException("ユーザー名かパスワードを指定しましょう")
                val reqURL = "https://f5.si/update.php?domain=${ddnsNowUser}&password=${ddnsNowPassword}"
                println("Requesting:$reqURL")
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url(reqURL)
                    .build()

                val r = try {
                    val response = client.newCall(request).execute()
                    response.body?.string()
                } catch (e: IOException) {
                    throw e
                }
                println(r)
                if (r == null || r.contains("ERROR")) {
                    println("エラーが発生しました。\nユーザー名やパスワードを確認しやがれください")
                } else if (r.contains("OK")) {
                    println("正常に更新しました。")
                }


            }

        }
        return
    }
}
