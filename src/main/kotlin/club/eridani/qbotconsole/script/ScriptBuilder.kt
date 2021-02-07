package club.eridani.qbotconsole.script

import club.eridani.qbotconsole.GLOBAL_CONSOLE
import club.eridani.qbotconsole.qqcommand.GeneralCommandExecutor
import club.eridani.qbotconsole.qqcommand.QCommandHandler
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.Listener

open class ScriptBuilder(val scriptId: String) {
    val botConsole = GLOBAL_CONSOLE
    val load = mutableListOf<() -> Unit>()
    val unload = mutableListOf<() -> Unit>()

    var botFilter: (Bot) -> Boolean = { true }

    val listeners: MutableList<Listener<*>>  = mutableListOf()
    val commands : MutableList<QCommandHandler<*, *, *>> = mutableListOf()

    @ScriptDSL
    fun load(block: () -> Unit) {
        load += block
    }

    @ScriptDSL
    fun unload(block: () -> Unit) {
        unload += block
    }


    fun onScriptLoad() {
        load.forEach {
            it()
        }

        commands.forEach { it.register() }
    }

    fun onScriptUnLoad() {
        unload.forEach { it() }
        listeners.forEach {
            it.complete()
        }

        listeners.clear()
        System.gc()

        commands.forEach {
            it.state = false
            it.stopListen()
        }
        commands.clear()
        System.gc()
    }

    @ScriptDSL
    fun bots(botRunner: Bot.() -> Unit) {
        botConsole.bots.filter(botFilter).forEach { load { botRunner(it) } }
    }

    @ScriptDSL
    fun botFilter(filter : (Bot) -> Boolean) {
        botFilter = filter
    }


    operator fun <E : Event> Listener<E>.unaryPlus() {
        listeners.add(this)
    }

    operator fun <E : QCommandHandler<*, *, *>> E.unaryPlus() {
        commands.add(this)
    }

    override fun toString(): String {
        return this.scriptId
    }

}

@ScriptDSL
fun script(scriptId: String, builder: ScriptBuilder.() -> Unit) = ScriptBuilder(scriptId).apply(builder).apply { GLOBAL_CONSOLE.scriptManager.scripts.add(this) }

fun Bot.generalCommand(name: String,  handler: suspend GeneralCommandExecutor.() -> Unit) = club.eridani.qbotconsole.qqcommand.generalCommand(name, bot, handler)