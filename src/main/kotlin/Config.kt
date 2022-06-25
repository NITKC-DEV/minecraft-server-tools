import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

val configFile = File("mstools.config.json")
const val AUTO_IP = "auto"

@kotlinx.serialization.Serializable
data class Config(
    val gitLocalRepo: String? = null,
    val gitRemoteRepo: String? = null,
    val sshSecretKeyPath: String? = null,
    val ddnsNowUser: String? = null,
    val ddnsNowPassword: String? = null,
    val ipAddress: String? = AUTO_IP,//AUTO_IPで自動判別
    val tcpPorts: List<Int>? = null,
    val udpPorts: List<Int>? = null
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun readConfig(): Config = Json.decodeFromStream(serializer(), configFile.inputStream())
    }
}

