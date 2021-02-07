package club.eridani.qbotconsole.util

import club.eridani.qbotconsole.BotConsole
import java.io.File
import java.net.URL

fun BotConsole.addJarToClasspath(jar: File) {
    val cl = ClassLoader.getSystemClassLoader()
    val clazz: Class<*> = cl.javaClass

    val method = clazz.superclass.getDeclaredMethod("addURL", URL::class.java)

    method.isAccessible = true
    method.invoke(cl, arrayOf<Any>(jar.toURI().toURL()))
}


fun BotConsole.downloadLib(url: String, libName: String) {
    File("${libraryDirectory.absolutePath}/$libName.jar").writeBytes(URL(url).readBytes())
}


fun BotConsole.loadAllLibInDir() {
    this.libraryDirectory.listFiles()!!.filter { it.absolutePath.endsWith(".jar") }.forEach {
        addJarToClasspath(it)
    }
}