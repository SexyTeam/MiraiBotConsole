package club.eridani.qbotconsole.command.builtin

import club.eridani.qbotconsole.GLOBAL_CONSOLE
import club.eridani.qbotconsole.VERSION
import club.eridani.qbotconsole.console.AccountStorage
import club.eridani.qbotconsole.console.consoleCommand
import club.eridani.qbotconsole.util.loadAllLibInDir

val version =
    consoleCommand("version") {
        reply("当前版本: $VERSION")
    }


val clear =
    consoleCommand("clear") {
        commandHandler.botConsole.consoleTextArea.text = ""
    }


val bot =
    consoleCommand("bot") {
        "add" {
            noArgRunner {
                reply("请检查弹出的新窗口")
                GLOBAL_CONSOLE.gui.openNewBotWindow()
            }

            requireArg(2) {
                GLOBAL_CONSOLE.addNewAccountStorageToFile(AccountStorage.Account(long(0)!!, text(1)!!))
                reply("添加了新的 QQ: ${long(0)}")
            }
        }

        "list" {
            reply("当前机器人列表: " + GLOBAL_CONSOLE.bots.toString())
        }

    }

val reloadLib = consoleCommand("lib") {
    "reload" {
        GLOBAL_CONSOLE.loadAllLibInDir()
    }
}


val help = consoleCommand("help") {
    reply(listOf(version, clear, bot, reloadLib).joinToString(", ") { it.command })
}