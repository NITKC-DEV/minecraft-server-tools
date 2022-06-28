import SyncMode.Pull
import SyncMode.Push
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import org.eclipse.jgit.api.CheckoutCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.errors.CheckoutConflictException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.sshd.SshdSessionFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.*

@OptIn(ExperimentalCli::class)
class Sync(config: Config?) : Subcommand("sync", "ワールドを同期するます") {
    private val mode by argument(ArgType.Choice<SyncMode>(), "Mode")
    private val localServerPath by option(
        ArgType.String,
        "local",
        "l",
        "リポジトリの同期先を指定するます"
    ).default(config?.serverDirectory.orEmpty())
    val remoteRepo by option(ArgType.String, "remote", "r", "リモートリポジトリのURL").default(config?.gitRemoteRepo.orEmpty())
    val sshKeyPath by option(
        ArgType.String,
        "key-path",
        description = "SSH秘密鍵のパス"
    ).default(config?.sshSecretKeyPath.orEmpty())

    override fun execute() {
        if (localServerPath.isEmpty() || remoteRepo.isEmpty()) {
            println("パスが無効です。")
            return
        }
        val local = Path(localServerPath)
        when (mode) {
            Pull -> {
                //新規 Clone
                if (!local.isDirectory(LinkOption.NOFOLLOW_LINKS)) {
                    if (local.exists()) {
                        println("${local.absolutePathString()}はディレクトリではありません")
                        return
                    }
                    println("${local.absolutePathString()}を新規作成します")
                    local.createDirectories()
                }
                if (local.listDirectoryEntries().isEmpty()) {
                    println("Cloneします")
                    Git.cloneRepository().setDirectory(local.toFile()).setBare(false).setURI(remoteRepo).call()
                    return
                }
                // 既存 PULL
                val builder = FileRepositoryBuilder()
                val repo =
                    builder.setGitDir(local.resolve(Constants.DOT_GIT).toFile()).readEnvironment().findGitDir().build()
                val git = Git(repo)
                try {
                    git.pull().setRemote("origin").setRemoteBranchName("main").call()
                } catch (e: CheckoutConflictException) {
                    e.printStackTrace()
                    ultimateConflictSolver(e, repo, git)
                } catch (e: Exception) {
                    println("ドッカン☆")
                    throw e
                } finally {
                    git.close()
                }
            }
            Push -> {
                val builder = FileRepositoryBuilder()
                val repo =
                    builder.setGitDir(local.resolve(Constants.DOT_GIT).toFile()).readEnvironment().findGitDir().build()
                val git = Git(repo)
                git.commit().setSign(false).setAll(true).setMessage("[MSTools] Sync")
                    .setAuthor("MSTools", "mstools@naotiki-apps.xyz").call()
                git.status().paths.forEach {
                    println(it)
                }

                try {
                    superUltimateFinallyPowerfulPush(git)
                } catch (e: CheckoutConflictException) {
                    e.printStackTrace()
                    ultimateConflictSolver(e, repo, git)
                } catch (e: Exception) {
                    throw e
                } finally {
                    git.close()
                }
            }
        }

    }

    private fun ultimateConflictSolver(e: CheckoutConflictException, repo: Repository, git: Git) {
        println(
            """
                        ----!警告!----
                        コンフリクトが起きています
                        破壊的な変更をする恐れがあるので慎重に操作を行いましょう
                        意味がわからない場合は管理者に助けを求めましょう
                        ワールドデータ消えても知らんからな？？？？
                        
                    """.trimIndent()
        )
        print("続行しますか (\"I can solve\"で続行/それ以外でPullをキャンセル(当店おすすめ、これ一択、これしかない) ):")
        if (readln() == "I can solve") {
            println(
                """
                            言ったな？？？解決しろよ？？？壊すなよ？？？
                            ❤️----コンフリクト解決モード----❤️
                            解消方法を以下から選ぶがよい
                            (one)ローカルの変更を優先しコミット(もとに戻せる)(書き込み権限が必要です)
                            (two)リモートの変更を優先し、自身の変更は破棄(自分の変更内容は失われます)
                            (cancel)キャンセル
                        """.trimIndent()
            )
            var selected = ""
            while (!selected.lowercase().let {
                    it == "one" || it == "two" || it == "cancel"
                }) {
                print("どれにしますか？( one or two or cancel ):")
                selected = readln()
            }
            when (selected) {
                "one" -> {
                    println("選択：(one)ローカルの変更を優先しコミット(自分の変更を保持できる)")
                    val sshKey = File(sshKeyPath)
                    if (!sshKey.isFile) {
                        throw IllegalArgumentException("SSHキーのパスが正しく設定されていません")
                    }
                    git.checkout().addPaths(e.conflictingPaths).setStage(CheckoutCommand.Stage.OURS).call()
                    println("コンフリクトした愉快なメンバーたち")
                    git.add().apply {
                        e.conflictingPaths.forEach {
                            println(it)
                            addFilepattern(it)
                        }
                    }.call()
                    println("修正Commitします")
                    git.commit().setSign(false).setMessage("[MSTools] コンフリクト解決 (強制ローカルプッシュ)")
                        .setAuthor("MSTools", "mstools@naotiki-apps.xyz").call()
                    println("Pushします")
                    try {
                        superUltimateFinallyPowerfulPush(git)
                    } catch (e: Exception) {
                        println("おめぇの書き込み権限ねーから！！！！！！")
                        println("ドッカン☆")
                        throw e
                    }
                    println("たぶん解決")
                }
                "two" -> {
                    println("選択：(two)リモートの変更を優先し、自身の変更は破棄(今の変更内容は失われます)")
                    repo.writeMergeCommitMsg(null)
                    repo.writeMergeHeads(null)
                    Git.wrap(repo).reset().setMode(ResetCommand.ResetType.HARD).call()
                }
                "cancel" -> {
                    println("キャンセルします")
                    return
                }
            }
        } else {
            println("ナイスな判断！！")
            println("キャンセルしました。必要なら管理者に助けを求めましょう")
            return
        }
    }

    //は？
    private fun superUltimateFinallyPowerfulPush(git: Git): MutableIterable<PushResult> {
        val sshKey = File(sshKeyPath)
        if (!sshKey.isFile) {
            throw IllegalArgumentException("SSHキーのパスが正しく設定されていません")
        }
        return git.push().setPushAll().setTransportConfigCallback {
            if (it is SshTransport) {
                it.sshSessionFactory = object : SshdSessionFactory() {
                    val SSH_DIR: Path = Path(".ssh")
                    private var privateKeyFile: Path? = Path(sshKeyPath)
                    override fun getSshDirectory(): File? {
                        return try {
                            Files.createDirectories(SSH_DIR).toFile()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            null
                        }
                    }

                    override fun getDefaultIdentities(sshDir: File?): List<Path?> {
                        return listOf(privateKeyFile)
                    }
                }
            }
        }.call()
    }
}


enum class SyncMode {
    Pull,
    Push,
}