package club.eridani.qbotconsole.console

import club.eridani.qbotconsole.BotConsole
import club.eridani.qbotconsole.GLOBAL_CONSOLE
import club.eridani.qbotconsole.command.CommandHandler
import club.eridani.qbotconsole.command.ExecutorInterface
import club.eridani.qbotconsole.command.marker.CommandDSL
import kotlinx.coroutines.runBlocking

@CommandDSL
fun consoleCommand(name: String, handler: suspend ConsoleCommandExecutor.() -> Unit) =
    ConsoleCommandHandler(name, GLOBAL_CONSOLE, handler)

class ConsoleCommandHandler(name: String, val botConsole: BotConsole, handler: suspend ConsoleCommandExecutor.() -> Unit) :
    CommandHandler<String, ConsoleCommandHandler, ConsoleCommandExecutor>(name, handler) {
    override fun execute(args: List<String>) {
        if (!state) return

        val block = ConsoleCommandExecutor(args, this@ConsoleCommandHandler)
        runBlocking { handler.invoke(block) }
        if ((args.isEmpty()) and (subCommands.size == 0)) {
            noArgRunner?.execute(args)
            return
        }

        val commands = (subCommands.joinToString(", ") { it.command })

        if (subCommands.size != 0) {

            val helpHandler = ConsoleCommandHandler("help", botConsole) {
                reply("以下为可用指令\n$commands")
            }
            if ((args.isEmpty())) {
                if (noArgRunner == null) noArgRunner = helpHandler
                noArgRunner?.execute(args)

                subCommands.clear()
            } else {
                subCommands
                    .filter { it.command.equals(args[0].toString(), true) }
                    .apply {
                        if (this.isEmpty()) {
                            if (subCommands.size != 0) {
                                val nocmdFound = ConsoleCommandHandler("help", botConsole) {
                                    reply("没有找到指令, 以下为可用指令\n$commands")
                                }
                                if (noCommandFoundRunner == null) noCommandFoundRunner = nocmdFound
                                noCommandFoundRunner?.execute(args)
                                subCommands.clear()
                                return
                            }
                        } else {
                            forEach {
                                it.execute(args.toMutableList().apply { runCatching { removeAt(0) } })
                            }
                        }
                    }
            }

        }

        subCommands.clear()

    }

    override fun register() {
        GLOBAL_CONSOLE.consoleCommands += this
    }

}

class ConsoleCommandExecutor(args: List<String>, val commandHandler: ConsoleCommandHandler) :
    ExecutorInterface<String, ConsoleCommandHandler, ConsoleCommandExecutor>(args, commandHandler) {
    override fun text(index: Int): String? {
        return args.getOrNull(index)?.toString()
    }

    override fun int(index: Int): Int? {
        return args.getOrNull(index)?.toString()?.toIntOrNull()
    }

    override fun long(index: Int): Long? {
        return args.getOrNull(index)?.toLongOrNull()
    }

    override fun double(index: Int): Double? {
        return args.getOrNull(index)?.toDoubleOrNull()
    }

    override fun boolean(index: Int): Boolean? {
        return args.getOrNull(index)?.toBoolean()
    }

    override fun reply(str: String) {
        println(str)
        commandHandler.botConsole.consoleTextArea.text += "\n$str"
    }


    @CommandDSL
    override fun String.invoke(block: suspend ConsoleCommandExecutor.() -> Unit) {
        subCommand(this, block)
    }

    @CommandDSL
    override fun subCommand(command: String, block: suspend ConsoleCommandExecutor.() -> Unit) {
        commandInstance.subCommands.add(ConsoleCommandHandler(command, commandHandler.botConsole, block))
    }

    @CommandDSL
    override fun noArgRunner(block: suspend ConsoleCommandExecutor.() -> Unit) {
        commandInstance.noArgRunner = ConsoleCommandHandler("help", commandHandler.botConsole, block)
    }

    override fun noCommandFind(block: suspend ConsoleCommandExecutor.() -> Unit) {
        commandInstance.noCommandFoundRunner = ConsoleCommandHandler("help", commandHandler.botConsole, block)
    }
}