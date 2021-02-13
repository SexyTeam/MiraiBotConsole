package club.eridani.qbotconsole.qqcommand.group

import club.eridani.qbotconsole.qqcommand.QCommandExecutor
import club.eridani.qbotconsole.qqcommand.QCommandHandler
import club.eridani.qbotconsole.util.toVarList
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.toMessageChain

class GroupCommandHandler(bot: Bot, name: String, val scope: CommandWorkingScope, handler: suspend GroupCommandExecutor.() -> Unit) :
    QCommandHandler<GroupMessageEvent, GroupCommandHandler, GroupCommandExecutor>(bot, name, handler) {
    var groupFilter : (Group) -> Boolean = { true }
    open var noPermission : MessageChain = PlainText("你没有权限").toMessageChain()
    override var listener: Listener<GroupMessageEvent> = bot
        .eventChannel
        .subscribeAlways {
            if (state) {
                if (scope.compareFunc(sender)) {
                    val arg = message.toMutableList().apply { removeFirst() }.toVarList()
                    if (arg.size > 0 && arg.first() is PlainText) {
                        if ((arg.first() as PlainText).content.equals(command, true)) {
                            execute(this, arg.toMutableList().apply { runCatching { removeAt(0) } })
                        }
                    }
                } else {
                    group.sendMessage(noPermission)
                }
            }
        }

    override fun register() {
        state = true
    }


    override fun execute(e: GroupMessageEvent, args: List<SingleMessage>) {
        val block = GroupCommandExecutor(e, args, this)
        runBlocking { handler.invoke(block) }
        if (args.isEmpty()) {
            if (subCommands.isEmpty()) {
                noArgRunner?.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
            } else {
                noCommandFoundRunner = noCommandFoundRunner ?: GroupCommandHandler(bot,"help", scope) { reply("可用指令列表\n${subCommands.joinToString(", ") { it.command }}") }
                noCommandFoundRunner?.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
            }
        } else {
            subCommands
                .filter { args[0].contentEquals(it.command, true) }
                .apply {
                    if (this.isEmpty()) {
                        noCommandFoundRunner = noCommandFoundRunner ?: GroupCommandHandler(bot, "help", scope) { reply("没有找到当前指令, 指令列表\n${subCommands.joinToString(", ") { it.command }}") }
                        noCommandFoundRunner?.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
                    } else {
                        forEach {
                            it.execute(e, args.toMutableList().apply { runCatching { removeAt(0) } })
                        }
                    }
                }
        }

        subCommands.clear()
        System.gc()
    }
}

fun groupCommand(name: String, bot: Bot, scope: CommandWorkingScope,  handler: suspend GroupCommandExecutor.() -> Unit) =
    GroupCommandHandler(bot, name, scope , handler)

class GroupCommandExecutor(event: GroupMessageEvent, args: List<SingleMessage>, commandInstance: club.eridani.qbotconsole.qqcommand.group.GroupCommandHandler) :
    QCommandExecutor<GroupMessageEvent, club.eridani.qbotconsole.qqcommand.group.GroupCommandHandler, GroupCommandExecutor>(event, args, commandInstance) {
    override fun subCommand(command: String, block: suspend GroupCommandExecutor.() -> Unit) {
        commandInstance.subCommands += club.eridani.qbotconsole.qqcommand.group.GroupCommandHandler(
            commandInstance.bot,
            command,
            commandInstance.scope,
            block
        )
    }

    override fun noArgRunner(block: suspend GroupCommandExecutor.() -> Unit) {
        commandInstance.noArgRunner = club.eridani.qbotconsole.qqcommand.group.GroupCommandHandler(
            commandInstance.bot,
            "help",
            commandInstance.scope,
            block
        )
    }

    override fun noCommandFind(block: suspend GroupCommandExecutor.() -> Unit) {
        commandInstance.noCommandFoundRunner = club.eridani.qbotconsole.qqcommand.group.GroupCommandHandler(
            commandInstance.bot,
            "help",
            commandInstance.scope,
            block
        )
    }
}

enum class CommandWorkingScope(val compareFunc: (Member) -> Boolean) {
    EVERYONE({ true }) { override val type = this } ,
    MEMBER_ONLY({ it.permission == net.mamoe.mirai.contact.MemberPermission.MEMBER }) { override val type = this },
    ADMIN_ONLY( { it.permission == net.mamoe.mirai.contact.MemberPermission.ADMINISTRATOR } ) { override val type = this },
    OWNER_ONLY( {it.permission == net.mamoe.mirai.contact.MemberPermission.OWNER} ) { override val type = this },
    MEMBER_AND_OWNER({ (it.permission == net.mamoe.mirai.contact.MemberPermission.MEMBER) or (it.permission == net.mamoe.mirai.contact.MemberPermission.OWNER) }) { override val type = this },
    MEMBER_AND_ADMINISTRATOR({ (it.permission == net.mamoe.mirai.contact.MemberPermission.MEMBER) or (it.permission == net.mamoe.mirai.contact.MemberPermission.ADMINISTRATOR) }) { override val type = this },
    ADMIN_AND_OWNER({ (it.permission == net.mamoe.mirai.contact.MemberPermission.ADMINISTRATOR) or (it.permission == net.mamoe.mirai.contact.MemberPermission.OWNER) }) { override val type = this };

    open val type: CommandWorkingScope by lazy { EVERYONE }
    open fun command(bot: Bot, name: String, handler: suspend GroupCommandExecutor.() -> Unit): GroupCommandHandler {
        return GroupCommandHandler(bot, name, type, handler)
    }
}