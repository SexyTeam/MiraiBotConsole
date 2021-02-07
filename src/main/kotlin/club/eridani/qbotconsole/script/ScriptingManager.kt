package club.eridani.qbotconsole.script

import club.eridani.qbotconsole.BotConsole
import club.eridani.qbotconsole.util.gradleKts
import tornadofx.asObservable
import java.io.File
import javax.script.ScriptEngineManager

class ScriptingManager(val botConsole: BotConsole) {
    fun refresh() {
        scripts.forEach {
            it.onScriptUnLoad()
        }
        scripts.clear()

        scriptStorePath
            .listFiles()!!
            .filter { it.absolutePath.endsWith(".kts") }
            .map { compile(it.readText()) }

    }


    val scriptStorePath = File("scripts")
    val predefindGradleKts = File("build.gradle.kts")
    val scripts = mutableListOf<ScriptBuilder>().asObservable()

    init {
        if (!scriptStorePath.exists()) scriptStorePath.mkdir()

        if (!predefindGradleKts.exists()) predefindGradleKts.apply { createNewFile() }.writeText(gradleKts)
    }



    val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    fun compile(str: String) {
        engine.eval(str)
    }
}


