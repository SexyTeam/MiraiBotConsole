import club.eridani.qbotconsole.qqcommand.commands
import club.eridani.qbotconsole.script.script
import kotlinx.coroutines.launch
import net.mamoe.mirai.message.data.toMessageChain

script("test") {
    load {
        println("Test Script Has Loaded")
    }

    unload {
        println("Test Script Has Unload")
    }

    bots {
        commands {
            "/" {
                groups {
                    moderators {
                        +"禁言" {
                            member(0)?.mute(int(1) ?: 0)
                        }

                        +"解禁言" {
                            member(0)?.mute(0)
                        }

                        +"群发" {
                            bot.groups.forEach {
                                launch { it.sendMessage(args.toMessageChain()) }
                            }
                        }
                    }
                }
            }
        }
    }


}