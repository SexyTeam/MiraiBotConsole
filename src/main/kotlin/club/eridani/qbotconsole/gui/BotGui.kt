package club.eridani.qbotconsole.gui

import club.eridani.qbotconsole.BotConsole
import club.eridani.qbotconsole.GLOBAL_CONSOLE
import club.eridani.qbotconsole.command.builtin.version
import club.eridani.qbotconsole.gui.bot.NewBotGui
import club.eridani.qbotconsole.util.applyMetroTheme
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.text.Font
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import jfxtras.styles.jmetro.Style
import kotlinx.coroutines.runBlocking
import tornadofx.*
import java.io.File

class BotGui : App(MainView::class, MyStylesheet::class) {
    override fun start(stage: Stage) {
        stage.isResizable = false
        super.start(stage)
        val bounds = Screen.getPrimary().visualBounds
        val x: Double = bounds.minX + (bounds.width - 1000.0) / 2
        val y: Double = bounds.minY + (bounds.height - 800.0) / 2
        stage.x = x
        stage.y = y
        stage.width = 1000.0
        stage.height = 800.0
    }

    override fun stop() {
        super.stop()
        GLOBAL_CONSOLE.bots.forEach { it.close() }
    }
}

class MainView : View("Bot") {
    override val root = borderpane()

    var currentTheme = "Windows Light"

    val botConsole = BotConsole(File("./"), this)

    init {
        with(root) {
            applyMetroTheme(Style.LIGHT)
        }
    }

    init {
        with(root) {
            top = menubar {
                menu("文件") {
                    items += MenuItem("添加账号").apply {
                        accelerator = KeyCombination.keyCombination("Shortcut+N")
                        action {
                            openNewBotWindow()
                        }
                    }
                    separator()
                    items += MenuItem("退出").apply {
                        accelerator = KeyCombination.keyCombination("Shortcut+Q")
                        action {
                            close()
                        }
                    }
                }

                menu("设置") {
                    val toggleGroup = ToggleGroup()
                    menu("配色") {
                        radiomenuitem("Windows Light") {
                            setToggleGroup(toggleGroup)
                            isSelected = true
                            setOnAction {
                                this@MainView.root.applyMetroTheme(Style.LIGHT)
                            }
                        }

                        radiomenuitem("Windows Dark") {
                            setToggleGroup(toggleGroup)
                            setOnAction {
                                currentTheme = "Windows Dark"
                                this@MainView.root.applyMetroTheme(Style.DARK)
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        with(root) {
            center = tabpane {
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                var selectedBot = botConsole.bots.firstOrNull()
                tab("机器人") {
                    hbox {
                        listview(botConsole.bots) {
                            onUserSelect(1) {
                                selectedBot = it
                            }
                            multiSelect(true)
                        }
                        flowpane {
                            vgap = 10.0
                            hgap = 5.0

                            style {
                                paddingLeft = 20.0
                                paddingTop = 20.0
                            }


                            button("登录 / 重新登录") {
                                action {
                                    runAsync {
                                        runBlocking { selectedBot?.login() }
                                    }
                                }
                            }
                            button("添加新账号") {
                                action {
                                    openNewBotWindow()
                                }
                            }
                            button("删除") {
                                action {
                                    if (selectedBot != null) botConsole.removeNewAccountStorageToFile(selectedBot!!)
                                }
                            }
                        }
                    }

                }
                tab("脚本") {
                    var selectedScript = botConsole.scriptManager.scripts.firstOrNull()
                    hbox {
                        listview(botConsole.scriptManager.scripts) {
                            onUserSelect(1) {
                                selectedScript = it
                            }
                            multiSelect(true)
                        }
                        flowpane {
                            vgap = 10.0
                            hgap = 5.0

                            style {
                                paddingLeft = 20.0
                                paddingTop = 20.0
                            }


                            button("加载") {
                                action {
                                    runAsync {
                                        selectedScript?.onScriptLoad()
                                    }
                                }
                            }
                            button("取消加载") {
                                action {
                                    runAsync {
                                        selectedScript?.onScriptUnLoad()
                                    }
                                }
                            }

                            button("刷新") {
                                action {
                                    runAsync { botConsole.scriptManager.refresh() }
                                }
                            }
                        }
                    }

                }
                tab("控制台") {
                    vbox {

                        style {
                            paddingLeft = 15.0
                            paddingRight = 15.0
                            paddingTop = 15.0
                            paddingBottom = 10.0
                        }

                        spacing = 20.0

                        textarea {
                            botConsole.consoleTextArea = this
                            fitToParentHeight()
                            editableProperty().value = false
                        }


                        textfield {
                            setOnKeyPressed {
                                when (it.code) {
                                    KeyCode.ENTER -> {
                                        if (this.text.isEmpty()) {
                                            return@setOnKeyPressed
                                        }
                                        val input = this.text
                                        val cmd = input.split(" ").first()
                                        val arg = input.split(" ").toMutableList().apply { removeFirst() }

                                        text = ""

                                        botConsole.consoleCommands.filter { c -> c.command.equals(cmd, true) }
                                            .forEach { c ->
                                                c.execute(arg)
                                            }
                                    }
                                }
                            }
                        }


                    }
                }
            }
        }
    }


    init {
        version
    }


    fun openNewBotWindow() {
        NewBotGui(botConsole, currentTheme).openModal(stageStyle = StageStyle.DECORATED)
    }

    fun openWebView(url : String) {
        WebView(url).openModal(StageStyle.DECORATED)
    }

}

class MyStylesheet : Stylesheet() {
    init {
        root {
            font = Font("Noto Sans CJK SC Regular", 9.5)
        }
    }
}


