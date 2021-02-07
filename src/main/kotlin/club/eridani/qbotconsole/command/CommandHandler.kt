package club.eridani.qbotconsole.command

abstract class CommandHandler<ARG, C : CommandHandler<ARG, C, Executor>, Executor : ExecutorInterface<ARG, C, Executor>>(
    val command: String,
    val handler: suspend Executor.() -> Unit,
    autoRegister : Boolean = true
) {

    open val subCommands = mutableListOf<C>()

    open var noArgRunner: C? = null

    open var noCommandFoundRunner: C? = null

    open var state = true


    abstract fun execute(args: List<ARG>)

    abstract fun register()

    init {
        if (autoRegister) {
            register()
        }
    }
}