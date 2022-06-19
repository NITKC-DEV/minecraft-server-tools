import kotlinx.cli.*
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.sshd.SshdSessionFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

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
"Super Turbo Intelligent Ultimate Dynamite Max Large Big Long Ultra Powerful Useful Helpful Better Best Good God Delicious Beautiful Most Much Many Strong String Extend Infinity Legend Plus Power Fire Final Protected Defender Minecraft Server Tools Version Really Perfect Stable Professional Enterprise Edition")
    println("""
       =BQgggggggB6.     .6BgggggggNBBHggggggHBBRHggggggQBX`bBHggggggHBBRggggggggggggggggggBBRgggggggggggggggggHBBBggggggggggggggggggBBBggggggggggggggggggRBBHgggggggggggggggggRBBHgggggggggggggggggRQ~ 
      -DM;^^^^^^!RBg-    !Bv^^^^^^;PBM!^^^^^^!HB2^^^^^^^TBBvM@;^^^^^^;BBY^^^^^^^^^^^^^^^^^;RBf^^^^^^^^^^^^^^^^^;#B8;^^^^^^^^^^^^^^^^^8BR^^^^^^^^^^^^^^^^^^2BQ/^^^^^^^^^^^^^^^^^>BBL^^^^^^^^^^^^^^^^^1Bo`
      tBr______^OBBBb,vdKM#;______+MBo^_____^tBBL_______kBBRBE_______*BB;_______^^^^^^^^^^+BBL_______^^^^^^^^^^;GB&;_______^^^_______TBB;___^^^^^__^^^^___>BBT_______^^^^^^^^^^;pBq;^^^^^_______^^^^^uR;
     ~RA_______^!+EBB@BU+!;^_____^]BRr______^6BH;_______!!iBB1_______0BR_______^gHHHHHHHHHQBBv_______YHHHHHHHHHHRB@;______;8H6^______!BBv___+&HHv__OHHP^__^MBq_______!&HHHHHHHHHRBBMHHHHA;______/WHHHMN!
    `Wg!_________1NBBBR*_________;@Bp^______!BBS__________mBR^______^BB#_______!BBBBBBBBBBBBBv_______uBBBBBBBBBBBBg!______;0Bg;______;RBP^__^Sw%#kkQZw2;___zBB!______^pBBBBBBBBBBBBBBBBBR/______;9BBBM; 
    LBY__________/TTTTL^_________LBB>_______oBB;_________^TTL_______!BBz_______;TTTTTTTTTTGBBv_______uBBBBBBBBBBBBQ+______^*TL;____1ASRBM;____;ZRBBBNL_____;RBS_______+TTTTTTTTTTABBBBBBB6_______+BBE.  
   ~0D^_________________________;QB8_______;BBD_______;uu*__________TBB>__________________ABBv_______uBBBBBBBBBBBBR|_______________!}IbBQ!_____SB9}uRO^_____GBR;_________________^&BBBBBBB>_______nB!   
   AN!______^tYi^____1YY;_______iBBv_______iBBv_______TBBv_________^UBR/______^*fYYYYYYYYYOBBv_______uBuTTTTTTTT6BBc_______+YY!_______xBRv_____!/!_^!/^_____cBBL_______!YYYYYYYYYYGBBBBBBB&;______^#&   
  !B{^______<RBu^___;&BO;______;pB@!______;GBM!______^5BR(+!^______;GB0!______^kBBBBBBBBBBBBR*_______uB~        ?BBJ^______JBBs^______LBBu^______;++!^______/QBk^______!HBBBBBBBBBBBBBBBBBBt^_____^vRJ  
 -MW!______;9BM+____7BBJ_______>RBo^______+MB5^______!qBBBBS^______!8BP;______;GBBBBBBBBBBBBM/_______oBH00000000NBBY^______tBBu^______/QBa;______!&BG;______;EB&!______^aBBBBBBBBBwlBBBBBBBO;______;ER^ 
 {B?_______>BBBRRRRRBBH;_______ABB1_______tBBY_______>BBBBBs_______*BBS_______^;;;;;;;;;;;kB0!_______;;;;;;;;;;;sBBY^______1RBA^______;8BD;_______PBR!_______VBB|_______*BBBBBBBBS`-RBBBBBBBL_______+Bo`
^RS^______;MBBBBBBBBBBi_______;RBO^_______8BR;_______JBBBBB!_______vBBn__________________;6B8!__________________(BBV^______;HBE;_______EBR!_______YBBv_______!BB5_______^0BBBBBBu`  vBBBBBBBR;_______3R;
SBYvvvvvvveBBBBBBBBBBRLvvvvvvvSBBAvvvvvvv)BBMvvvvvvvvDBBBBRLvvvvvvv5BB2vvvvvvvvvvvvvvvvvvi@BHtvvvvvvvvvvvvvvvvvvABBPvvvvvvvLMBQ(vvvvvvv#BBVvvvvvvvyBBGvvvvvvvvRBBLvvvvvvvZBhLLL>`    &BBBBBBBSvvvvvvvJR9
.AB0mEEEEEE8BBBBBBBBBBBqEEEEEEEMBB8EEEEEEE&BBgEEEEEEEDBBBBBHEEEEEEE#BBQEEEEEEEEEEEEEEEEEEEQBROEEEEEEEEEEEEEEEEEE0BBgEEEEEEEWBBMEEEEEEEdRBRKEEEEEEmRBBOEEEEEEEHBB8EEEEEEE&BS.         |BBBBBB&EEEEEEE&BZ-
  /MRdEEEEEEbRBBBNSooopBWEEEEEEEMBBWEEEEEEEHBB8EEEEEEEHBBMGBOEEEEEEEQBBqEEEEEEEEEEEEEEEEEEMBB8EEEEEEEEEEEEEEEEEEgBB&EEEEEEEgBB@EEEEEEE@BB0EEEEEEE&BBgEEEEEEE&BBgEEEEEEE8BG`          :&BBBB#EEEEEEmMR(` 
   -kBgEEEEEEEgBBz    `3BgEEEEEEEgBB8EEEEEEEMBBOEEEEEEERBV-NNEEEEEEEqBBHEEEEEEEEEEEEEEEEEEHBB&EEEEEEEEEEEEEEEEEEMBBDEEEEEEmQBBdEEEEEEmRBRKEEEEEEEBBREEEEEEEdBBQEEEEEEE8BS             sBBQKEEEEEEWRW^   
     1QBRRRRRRRBR-     `LBBRRRRRRRBBBRRRRRRRRBBBRRRRRRRBB! vBRRRRRRRRBBBRRRRRRRRRRRRRRRRRRBBBBRRRRRRRRRRRRRRRRRRBBBRRRRRRRRBBBRRRRRRRRBBBRRRRRRRBBBBRRRRRRRBBBRRRRRRRRB9.             -BBRRRRRRRRBu.    

    """.trimIndent())
    val parser = ArgParser("mstools")
    val isInitialized = configFile.exists()
    val config = if (isInitialized) Config.readConfig() else null

    class Debug : Subcommand("debug", "デバッグ用") {
        override fun execute() {

        }

        infix fun String.be(string: String) {

        }

        infix fun String.want(string: String): String {
            println("$this love $string")
            return ""
        }


    }

    class Initialize : Subcommand("init", "設定ファイルを生成して初期化します") {
        val default by option(ArgType.Boolean, "default", "d", "質問なしで設定ファイルを生成します").default(false)
        override fun execute() {
            if (isInitialized) {
                println(
                    """
                    すでに初期化されています。
                    ${configFile.absolutePath}を削除するか直接中身を書き換えてください
                """.trimIndent()
                )
                println("")
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
                        prompt("管理者から渡されたSSHキーの（絶対・相対）パスを入力 (設定しない場合はそのままEnter):")
                    } else null
                    //DDNS now設定
                    val ddnsName = prompt("DDNS nowユーザー名を入力 (設定しない場合はそのままEnter):")
                    val ddnsPassword = if (ddnsName != null) {
                        prompt("DDNS nowパスワード(APIトークン)を入力 (設定しない場合はそのままEnter):")
                    } else null
                    val ipAddress = prompt("サーバーのIPv4アドレスを入力（デフォルトでは自動で設定されます）:") ?: "auto"
                    val ports = getPorts()

                    Config(
                        gitLocalRepo,
                        gitRemoteRepo,
                        sshKeyPath,
                        ddnsName,
                        ddnsPassword,
                        ipAddress,
                        ports?.first?.toTypedArray(),
                        ports?.second?.toTypedArray()
                    )

                } else Config()
                outFile.write(json.encodeToString(Config.serializer(), conf).encodeToByteArray())
                outFile.close()
            }
            println("設定ファイルの書き方は https://github.com/NITKC22s/minecraft-server-tools/docs/ConfigFile.md を見てください")
        }

    }


    val sync = Sync(config)
    val debug = Debug()
    val network = Network()
    val init = Initialize()
    parser.subcommands(sync, debug, network, init)
    parser.parse(args)
    return
}


