package club.eridani.qbotconsole.command

import club.eridani.qbotconsole.command.marker.CommandDSL
import club.eridani.qbotconsole.command.marker.ExecutorDSL

abstract class ExecutorInterface<ARG, C : CommandHandler<ARG, C, Executor>, Executor: ExecutorInterface<ARG, C, Executor>>(
    val args: List<ARG>,
    val commandInstance: C,
) {
    abstract fun text(index: Int): String?

    abstract fun int(index: Int): Int?

    abstract fun long(index: Int): Long?

    abstract fun double(index: Int): Double?

    abstract fun boolean(index: Int): Boolean?


    @CommandDSL
    open operator fun String.invoke(block: suspend Executor.() -> Unit) =
        subCommand(this, block)

    @CommandDSL
    abstract fun subCommand(command: String, block: suspend Executor.() -> Unit)

    @CommandDSL
    abstract fun noArgRunner(block: suspend Executor.() -> Unit)

    @CommandDSL
    abstract fun noCommandFind(block: suspend Executor.() -> Unit)

    abstract fun reply(str: String)


    @ExecutorDSL
    open fun requireArg(size: Int, message: String? = null, block: () -> Unit) {
        if (args.size >= size) {
            block()
        } else {
            if(message != null) reply(message)
        }
    }
}

