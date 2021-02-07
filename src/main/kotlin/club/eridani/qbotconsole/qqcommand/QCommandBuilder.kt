package club.eridani.qbotconsole.qqcommand

import club.eridani.qbotconsole.command.CommandHandler
import club.eridani.qbotconsole.command.ExecutorInterface
import club.eridani.qbotconsole.command.marker.CommandDSL
import club.eridani.qbotconsole.qqcommand.group.CommandWorkingScope
import club.eridani.qbotconsole.qqcommand.group.GroupCommandExecutor
import club.eridani.qbotconsole.qqcommand.group.GroupCommandHandler
import net.mamoe.mirai.Bot

class QCommandBuilder(val bot: Bot) {
    @CommandDSL operator fun String.invoke(prefixScope: PrefixScope.() -> Unit) = PrefixScope(bot, this).apply(prefixScope)
}


class PrefixScope(val bot: Bot, val prefix: String) : Scope<GeneralCommandExecutor, GeneralCommandHandler>{
    @CommandDSL override operator fun String.invoke(handler: suspend GeneralCommandExecutor.() -> Unit) = GeneralCommandHandler(bot, "${prefix}$this", handler)

    class GroupCommand(val bot: Bot, val prefixScope: PrefixScope, val scope: CommandWorkingScope = CommandWorkingScope.EVERYONE) : Scope<GroupCommandExecutor, GroupCommandHandler> {
        override fun String.invoke(handler: suspend GroupCommandExecutor.() -> Unit)= GroupCommandHandler(bot, "${prefixScope.prefix}$this", scope , handler)


        @CommandDSL fun everyone(b : GroupCommand.() -> Unit) = GroupCommand(bot, prefixScope, CommandWorkingScope.EVERYONE).apply(b)
        @CommandDSL fun admins(b : GroupCommand.() -> Unit) = GroupCommand(bot, prefixScope, CommandWorkingScope.ADMIN_ONLY).apply(b)
        @CommandDSL fun owner(b : GroupCommand.() -> Unit) = GroupCommand(bot, prefixScope, CommandWorkingScope.OWNER_ONLY).apply(b)
        @CommandDSL fun memberAndOwner(b : GroupCommand.() -> Unit) = GroupCommand(bot, prefixScope, CommandWorkingScope.MEMBER_AND_OWNER).apply(b)
        @CommandDSL fun memberAndAdmin(b : GroupCommand.() -> Unit) = GroupCommand(bot, prefixScope, CommandWorkingScope.MEMBER_AND_ADMINISTRATOR).apply(b)
        @CommandDSL fun members(b : GroupCommand.() -> Unit) = GroupCommand(bot, prefixScope, CommandWorkingScope.MEMBER_ONLY).apply(b)
        @CommandDSL fun operators(b : GroupCommand.() -> Unit) = GroupCommand(bot, prefixScope, CommandWorkingScope.ADMIN_AND_OWNER).apply(b)
    }

    @CommandDSL fun groups(b : GroupCommand.() -> Unit) = GroupCommand(bot, this, CommandWorkingScope.EVERYONE).apply(b)
}

@CommandDSL
fun Bot.commands(b: QCommandBuilder.() -> Unit) = QCommandBuilder(this).apply(b)

interface Scope<E : ExecutorInterface<*,*,*>, H : CommandHandler<*,*,*>> {
    @CommandDSL operator fun String.invoke(handler: suspend E.() -> Unit) : H
}
