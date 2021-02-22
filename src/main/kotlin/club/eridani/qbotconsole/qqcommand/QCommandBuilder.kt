package club.eridani.qbotconsole.qqcommand

import club.eridani.qbotconsole.command.CommandHandler
import club.eridani.qbotconsole.command.ExecutorInterface
import club.eridani.qbotconsole.command.marker.CommandDSL
import club.eridani.qbotconsole.qqcommand.group.CommandWorkingScope
import club.eridani.qbotconsole.qqcommand.group.GroupCommandExecutor
import club.eridani.qbotconsole.qqcommand.group.GroupCommandHandler
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group

class QCommandBuilder(val bot: Bot) {
    @CommandDSL operator fun String.invoke(prefixScope: PrefixScope.() -> Unit) = PrefixScope(bot, this).apply(prefixScope)
}


class PrefixScope(val bot: Bot, val prefix: String) : Scope<GeneralCommandExecutor, GeneralCommandHandler>{
    @CommandDSL override operator fun String.invoke(handler: suspend GeneralCommandExecutor.() -> Unit) = GeneralCommandHandler(bot, "${prefix}$this", handler)

    class GroupCommandScope(val bot: Bot, val prefixScope: PrefixScope, val scope: CommandWorkingScope = CommandWorkingScope.EVERYONE, var groupFilter : (Group) -> Boolean = { true }) : Scope<GroupCommandExecutor, GroupCommandHandler> {
        @CommandDSL override fun String.invoke(handler: suspend GroupCommandExecutor.() -> Unit)= GroupCommandHandler(bot, "${prefixScope.prefix}$this", scope , handler).apply { this.groupFilter = this@GroupCommandScope.groupFilter }
        @CommandDSL fun everyone(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, prefixScope, CommandWorkingScope.EVERYONE, groupFilter).apply(b)
        @CommandDSL fun admins(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, prefixScope, CommandWorkingScope.ADMIN_ONLY, groupFilter).apply(b)
        @CommandDSL fun owner(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, prefixScope, CommandWorkingScope.OWNER_ONLY, groupFilter).apply(b)
        @CommandDSL fun memberAndOwner(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, prefixScope, CommandWorkingScope.MEMBER_AND_OWNER, groupFilter).apply(b)
        @CommandDSL fun memberAndAdmin(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, prefixScope, CommandWorkingScope.MEMBER_AND_ADMINISTRATOR, groupFilter).apply(b)
        @CommandDSL fun members(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, prefixScope, CommandWorkingScope.MEMBER_ONLY, groupFilter).apply(b)
        @CommandDSL fun moderators(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, prefixScope, CommandWorkingScope.ADMIN_AND_OWNER, groupFilter).apply(b)

        @CommandDSL fun groupFilter(groupFilter: (Group) -> Boolean) = this.apply { this.groupFilter = groupFilter }
        operator fun invoke(b: GroupCommandScope.() -> Unit) = this.apply(b)
    }

    @CommandDSL fun groups(b : GroupCommandScope.() -> Unit) = GroupCommandScope(bot, this, CommandWorkingScope.EVERYONE).apply(b)
}

@CommandDSL
fun Bot.commands(b: QCommandBuilder.() -> Unit) = QCommandBuilder(this).apply(b)

interface Scope<E : ExecutorInterface<*,*,*>, H : CommandHandler<*,*,*>> {
    @CommandDSL operator fun String.invoke(handler: suspend E.() -> Unit) : H
}


fun main(args: Array<String>) {
    println(readLine()!!.toInt() + readLine()!!.toInt())
}