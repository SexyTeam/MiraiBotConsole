package club.eridani.qbotconsole.util

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage

fun Collection<SingleMessage>.toVarList() : MessageChain {
    val new = MessageChainBuilder()
    this.forEach {
        when(it) {
            is PlainText -> {
                it.content.split(" ").forEach { s ->
                    new.add(PlainText(s))
                }
            }
            else -> new.add(it)
        }
    }

    return new.asMessageChain()
}