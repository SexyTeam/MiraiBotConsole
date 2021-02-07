package club.eridani.qbotconsole.util

import javafx.scene.Parent
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import tornadofx.style

fun Parent.applyMetroTheme(style: Style = Style.DARK) {
    val jmetro = JMetro(style)
    jmetro.parent = this
    style {
        styleClass.add("background")
    }


}