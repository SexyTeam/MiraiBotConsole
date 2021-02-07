import club.eridani.qbotconsole.qqcommand.commands
import club.eridani.qbotconsole.script.script

script("test") {
    load {
        println("Test Script Has Loaded")
    }

    unload {
        println("Test Script Has Unload")
    }

    bots {
        commands {
            "!" {
                +"test" {
                    reply("test")
                }

                groups {
                    groupFilter { it.members.contains(1962226235) }


                    +"a" {
                        reply("a")
                    }

                    +"b" {
                        "a" {
                            reply("ba")
                        }

                        "b" {
                            reply("bb")
                        }
                    }

                    members {
                        +"member" {
                            reply("Hello Member")
                        }
                    }
                }

            }
        }
    }


}