package club.eridani.qbotconsole.qqcommand

import club.eridani.qbotconsole.util.toVarList
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage

class GeneralCommandHandler(bot: Bot, name: String, handler: suspend GeneralCommandExecutor.() -> Unit) :
    QCommandHandler<MessageEvent, GeneralCommandHandler, GeneralCommandExecutor>(bot, name, handler) {

    override var listener: Listener<MessageEvent> = bot
        .eventChannel
        .subscribeAlways<MessageEvent> {
            when {
                state -> {
                    val arg = message.toMutableList().apply { removeFirst() }.toVarList()
                    when {
                        arg.size > 0 && arg.first() is PlainText -> {
                            when {
                                (arg.first() as PlainText).content.equals(command, true) -> {
                                    execute(this, arg.toMutableList().apply { runCatching { removeAt(0) } })
                                }
                            }
                        }
                    }
                }
            }
        }

    override fun register() {
        state = true
    }


    override fun execute(e: MessageEvent, args: List<SingleMessage>) {
        val block = GeneralCommandExecutor(e, args, this)
        runBlocking { handler.invoke(block) }
        when {
            args.isEmpty() -> {
                when {
                    subCommands.isEmpty() -> {
                        noArgRunner?.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
                    }
                    else -> {
                        noCommandFoundRunner = noCommandFoundRunner ?: GeneralCommandHandler(bot,
                            "help") { reply("可用指令列表\n${subCommands.joinToString(", ") { it.command }}") }
                        noCommandFoundRunner?.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
                    }
                }
            }
            else -> {
                subCommands
                    .filter { args[0].contentEquals(it.command, true) }
                    .apply {
                        when {
                            this.isEmpty() -> {
                                noCommandFoundRunner = noCommandFoundRunner ?: GeneralCommandHandler(bot,
                                    "help") { reply("没有找到当前指令, 指令列表\n${subCommands.joinToString(", ") { it.command }}") }
                                noCommandFoundRunner?.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
                            }
                            else -> {
                                forEach {
                                    it.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
                                }
                            }
                        }
                    }
            }
        }

        subCommands.clear()
        System.gc()
    }
}

fun generalCommand(name: String, bot: Bot, handler: suspend GeneralCommandExecutor.() -> Unit) =
    GeneralCommandHandler(bot, name, handler)

class GeneralCommandExecutor(event: MessageEvent, args: List<SingleMessage>, commandInstance: club.eridani.qbotconsole.qqcommand.GeneralCommandHandler) :
    QCommandExecutor<MessageEvent, club.eridani.qbotconsole.qqcommand.GeneralCommandHandler, GeneralCommandExecutor>(event, args, commandInstance) {
    override fun subCommand(command: String, block: suspend GeneralCommandExecutor.() -> Unit) {
        commandInstance.subCommands += club.eridani.qbotconsole.qqcommand.GeneralCommandHandler(
            commandInstance.bot,
            command,
            block
        )
    }

    override fun noArgRunner(block: suspend GeneralCommandExecutor.() -> Unit) {
        commandInstance.noArgRunner =
            club.eridani.qbotconsole.qqcommand.GeneralCommandHandler(commandInstance.bot, "help", block)
    }

    override fun noCommandFind(block: suspend GeneralCommandExecutor.() -> Unit) {
        commandInstance.noCommandFoundRunner =
            club.eridani.qbotconsole.qqcommand.GeneralCommandHandler(commandInstance.bot, "help", block)
    }
}