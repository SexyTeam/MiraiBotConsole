package club.eridani.qbotconsole.gui

import club.eridani.qbotconsole.util.miraiJs
import tornadofx.View
import tornadofx.webview

//Mirai selenium
class WebView(val url: String, val useMiraiJs : Boolean = false) : View(url) {
    override val root = webview {
        engine.userAgent =
            "Mozilla/5.0 (Linux; {Android Version}; {Build Tag etc.} AppleWebKit/{WebKit Rev} (KHTML, like Gecko) Chrome/{Chrome Rev} Safari/{WebKit Rev}"
        engine.load(url)
        if (useMiraiJs) engine.executeScript(miraiJs)
    }
}