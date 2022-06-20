import kotlinx.cli.*
import kotlinx.serialization.json.Json

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
"Super Turbo Intelligent Ultimate Dynamite Max Large Big Long Ultra Powerful Useful Helpful Better Best Good God Delicious Beautiful Most Much Many Strong String Extend Infinity Legend Plus Power Fire Final Protected Defender Minecraft Server Tools Version Really Perfect Stable Long Term Support Professional Enterprise Edition")
    println("""
       =BQgggggggB6.     .6BgggggggNBBHggggggHBBRHggggggQBX`bBHggggggHBBRggggggggggggggggggBBRgggggggggggggggggHBBBggggggggggggggggggBBBggggggggggggggggggRBBHgggggggggggggggggRBBHgggggggggggggggggRQ~ 
      -DM;^^^^^^!RBg-    !Bv^^^^^^;PBM!^^^^^^!HB2^^^^^^^TBBvM@;^^^^^^;BBY^^^^^^^^^^^^^^^^^;RBf^^^^^^^^^^^^^^^^^;#B8;^^^^^^^^^^^^^^^^^8BR^^^^^^^^^^^^^^^^^^2BQ/^^^^^^^^^^^^^^^^^>BBL^^^^^^^^^^^^^^^^^1Bo`
      tBr      ^OBBBb,vdKM#;      +MBo^     ^tBBL       kBBRBE       *BB;       ^^^^^^^^^^+BBL       ^^^^^^^^^^;GB&;       ^^^       TBB;   ^^^^^  ^^^^   >BBT       ^^^^^^^^^^;pBq;^^^^^       ^^^^^uR;
     ~RA       ^!+EBB@BU+!;^     ^]BRr      ^6BH;       !!iBB1       0BR       ^gHHHHHHHHHQBBv       YHHHHHHHHHHRB@;      ;8H6^      !BBv   +&HHv  OHHP^  ^MBq       !&HHHHHHHHHRBBMHHHHA;      /WHHHMN!
    `Wg!         1NBBBR*         ;@Bp^      !BBS          mBR^      ^BB#       !BBBBBBBBBBBBBv       uBBBBBBBBBBBBg!      ;0Bg;      ;RBP^  ^Sw%#kkQZw2;   zBB!      ^pBBBBBBBBBBBBBBBBBR/      ;9BBBM; 
    LBY          /TTTTL^         LBB>       oBB;         ^TTL       !BBz       ;TTTTTTTTTTGBBv       uBBBBBBBBBBBBQ+      ^*TL;    1ASRBM;    ;ZRBBBNL     ;RBS       +TTTTTTTTTTABBBBBBB6       +BBE.  
   ~0D^                         ;QB8       ;BBD       ;uu*          TBB>                  ABBv       uBBBBBBBBBBBBR|               !}IbBQ!     SB9}uRO^     GBR;                 ^&BBBBBBB>       nB!   
   AN!      ^tYi^    1YY;       iBBv       iBBv       TBBv         ^UBR/      ^*fYYYYYYYYYOBBv       uBuTTTTTTTT6BBc       +YY!       xBRv     !/! ^!/^     cBBL       !YYYYYYYYYYGBBBBBBB&;      ^#&   
  !B{^      <RBu^   ;&BO;      ;pB@!      ;GBM!      ^5BR(+!^      ;GB0!      ^kBBBBBBBBBBBBR*       uB~        ?BBJ^      JBBs^      LBBu^      ;++!^      /QBk^      !HBBBBBBBBBBBBBBBBBBt^     ^vRJ  
 -MW!      ;9BM+    7BBJ       >RBo^      +MB5^      !qBBBBS^      !8BP;      ;GBBBBBBBBBBBBM/       oBH00000000NBBY^      tBBu^      /QBa;      !&BG;      ;EB&!      ^aBBBBBBBBBwlBBBBBBBO;      ;ER^ 
 {B?       >BBBRRRRRBBH;       ABB1       tBBY       >BBBBBs       *BBS       ^;;;;;;;;;;;kB0!       ;;;;;;;;;;;sBBY^      1RBA^      ;8BD;       PBR!       VBB|       *BBBBBBBBS`-RBBBBBBBL       +Bo`
^RS^      ;MBBBBBBBBBBi       ;RBO^       8BR;       JBBBBB!       vBBn                  ;6B8!                  (BBV^      ;HBE;       EBR!       YBBv       !BB5       ^0BBBBBBu`  vBBBBBBBR;       3R;
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
                    val ports = getPorts()

                    Config(
                        gitLocalRepo,
                        gitRemoteRepo,
                        sshKeyPath,
                        ddnsName,
                        ddnsPassword,
                        ipAddress,
                        ports?.first,
                        ports?.second
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


