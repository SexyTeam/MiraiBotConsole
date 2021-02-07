package club.eridani.qbotconsole.qqcommand

import club.eridani.qbotconsole.command.CommandHandler
import club.eridani.qbotconsole.command.ExecutorInterface
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.SingleMessage

abstract class QCommandExecutor<E : MessageEvent, QCMD : QCommandHandler<E, QCMD, QCMDE>, QCMDE : QCommandExecutor<E, QCMD, QCMDE>>(
    val e: E,
    args: List<SingleMessage>,
    commandInstance: QCMD,
) :
    ExecutorInterface<SingleMessage, QCMD, QCMDE>(args, commandInstance) {

    override fun text(index: Int): String? {
        return args.getOrNull(index)?.contentToString()
    }

    override fun int(index: Int): Int? {
        return args.getOrNull(index)?.contentToString()?.toIntOrNull()
    }

    override fun long(index: Int): Long? {
        return args.getOrNull(index)?.contentToString()?.toLongOrNull()
    }

    override fun double(index: Int): Double? {
        return args.getOrNull(index)?.contentToString()?.toDoubleOrNull()
    }

    override fun boolean(index: Int): Boolean? {
        return args.getOrNull(index)?.contentToString()?.toBoolean()
    }

    override fun reply(str: String) {
        runBlocking { e.subject.sendMessage(str) }
    }
}


abstract class QCommandHandler<E : MessageEvent, QCMD : QCommandHandler<E, QCMD, QCMDE>, QCMDE : QCommandExecutor<E, QCMD, QCMDE>>(
    val bot: Bot,
    command: String,
    handler: suspend QCMDE.() -> Unit,
) : CommandHandler<SingleMessage, QCMD, QCMDE>(command, handler, false) {
    abstract var listener: Listener<E>
    abstract fun execute(e: E, args: List<SingleMessage>)
    override fun execute(args: List<SingleMessage>) {
        error("Check Code Invalid Call")
    }

    override var state = false

    open fun stopListen() {
        listener.complete()
        System.gc()
    }
}