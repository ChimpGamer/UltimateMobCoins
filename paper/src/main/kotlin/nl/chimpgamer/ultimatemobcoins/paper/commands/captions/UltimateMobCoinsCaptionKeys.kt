package nl.chimpgamer.ultimatemobcoins.paper.commands.captions

import org.incendo.cloud.caption.Caption
import java.util.LinkedList

object UltimateMobCoinsCaptionKeys {
    private val RECOGNIZED_CAPTIONS: MutableCollection<Caption> = LinkedList()

    val ARGUMENT_PARSE_FAILURE_MENU = of("argument.parse.failure.menu")

    private fun of(key: String): Caption {
        val caption = Caption.of(key)
        RECOGNIZED_CAPTIONS.add(caption)
        return caption
    }
}