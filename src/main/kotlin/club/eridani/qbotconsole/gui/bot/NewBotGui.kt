package club.eridani.qbotconsole.gui.bot

import club.eridani.qbotconsole.BotConsole
import club.eridani.qbotconsole.console.AccountStorage
import club.eridani.qbotconsole.util.applyMetroTheme
import javafx.beans.property.SimpleStringProperty
import jfxtras.styles.jmetro.Style
import tornadofx.*

class NewBotGui(val botConsole: BotConsole, val theme: String) : Fragment("添加新账号") {


    override val root = vbox {
        spacing = 10.0
        this.applyMetroTheme(if (theme == "Windows Light") Style.LIGHT else Style.DARK)
        style {
            paddingLeft = 20.0
            paddingTop = 15.0
            paddingRight = 20.0
            paddingBottom = 15.0
        }
        val acc = SimpleStringProperty()
        val pwd = SimpleStringProperty()
        form {
            fieldset("添加新账号") {
                field("账号") {
                    textfield(acc) {
                        filterInput { it.text.lastOrNull()?.toString()?.isInt() ?: false }
                    }
                }

                field("密码") {
                    passwordfield(pwd)
                }
            }

            buttonbar {
                button("添加") {
                    action {
                        if (acc.value != null && pwd.value != null) {
                            runAsync {
                                botConsole.addNewAccountStorageToFile(AccountStorage.Account(acc.value.toLong(), pwd.value))
                            } ui {
                                close()
                            }
                        }
                    }

                }
            }
        }



    }
}