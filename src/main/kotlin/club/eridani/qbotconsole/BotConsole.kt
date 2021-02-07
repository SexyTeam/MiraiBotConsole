package club.eridani.qbotconsole

import club.eridani.qbotconsole.command.CommandHandler
import club.eridani.qbotconsole.console.AccountStorage
import club.eridani.qbotconsole.console.ConsoleCommandExecutor
import club.eridani.qbotconsole.console.ConsoleCommandHandler
import club.eridani.qbotconsole.gui.BotGui
import club.eridani.qbotconsole.gui.MainView
import club.eridani.qbotconsole.script.ScriptingManager
import club.eridani.qbotconsole.util.loadAllLibInDir
import com.charleskorn.kaml.Yaml
import javafx.scene.control.TextArea
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import tornadofx.asObservable
import tornadofx.launch
import tornadofx.runAsync
import tornadofx.ui
import java.io.File

/**
 * 这个玩意儿有 GUI 的, 要是有人想一个vm开两个这个人就像个傻逼,
 */
lateinit var GLOBAL_CONSOLE: BotConsole

const val VERSION = "1.0.2"

class BotConsole(val workspacePath: File, val gui: MainView) {
    val bots = mutableListOf<Bot>().asObservable()
    val accountsFile = File("${workspacePath.absolutePath}/accounts.yaml")
    val scriptManager = ScriptingManager(this)
    val libraryDirectory = File("libs")

    init {
        if (!libraryDirectory.exists()) libraryDirectory.mkdir()
    }

    init {
        loadAllLibInDir()
    }


    val consoleCommands = mutableListOf<CommandHandler<String, ConsoleCommandHandler, ConsoleCommandExecutor>>()

    lateinit var consoleTextArea: TextArea

    init {
        runAsync {
            if (!accountsFile.exists()) {
                accountsFile.createNewFile()
                accountsFile.writeText(Yaml.default.encodeToString(AccountStorage.serializer(),
                    AccountStorage(listOf())))
            }
        }
    }


    init {
        runAsync {
            Yaml.default.decodeFromString(AccountStorage.serializer(), accountsFile.readText()).accounts.forEach {
                bots.add(BotFactory.newBot(it.id, it.password) {
                    fileBasedDeviceInfo("${workspacePath.absolutePath}/dev.json")
                })
            }
        }
    }

    init {
        GLOBAL_CONSOLE = this
    }

    init {
        scriptManager.refresh()
    }


    fun addNewAccountStorageToFile(account: AccountStorage.Account) {
        Yaml.default.encodeToString(AccountStorage.serializer(), AccountStorage(
            Yaml.default.decodeFromString(AccountStorage.serializer(), accountsFile.readText()).accounts.toMutableList()
                .apply {
                    this.add(account)
                }
        )).apply {
            accountsFile.writeText(this)
        }


        runAsync {} ui {
            bots.add(BotFactory.newBot(account.id, account.password) {
                fileBasedDeviceInfo("dev.json")
            })
        }


    }

    fun removeNewAccountStorageToFile(bot: Bot) {
        Yaml.default.encodeToString(AccountStorage.serializer(), AccountStorage(
            Yaml.default.decodeFromString(AccountStorage.serializer(), accountsFile.readText()).accounts.toMutableList()
                .apply {
                    this.removeIf { it.id == bot.id }
                }
        )).apply {
            accountsFile.writeText(this)
        }



        bots.removeIf { it == bot }
    }
}

fun main() {
    launch<BotGui>()
}