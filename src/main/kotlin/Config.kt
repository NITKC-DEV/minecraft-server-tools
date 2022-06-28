import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

val configFile = File("mstools.config.json")
const val AUTO_IP = "auto"
 val DEFAULT_ARGS=listOf("-Xmx2G","-Xms2G")

@kotlinx.serialization.Serializable
data class Config(
    val serverDirectory: String = "./",
    val gitRemoteRepo: String? = null,
    val sshSecretKeyPath: String? = null,
    val ddnsNowUser: String? = null,
    val ddnsNowPassword: String? = null,
    val ipAddress: String? = AUTO_IP,//AUTO_IPで自動判別
    val tcpPorts: List<Int> = emptyList(),
    val udpPorts: List<Int> = emptyList(),
    val serverJar:String?=null,
    val jvmArgs:List<String> = DEFAULT_ARGS
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun readConfig(): Config = Json.decodeFromStream(serializer(), configFile.inputStream())
    }
}

