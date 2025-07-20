package nl.chimpgamer.ultimatemobcoins.paper.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class MiniString(val value: String, private val miniMessage: MiniMessage = DEFAULT_MINI_MESSAGE, private val placeholders: MutableList<TagResolver> = mutableListOf()) : ComponentLike {

    companion object {
        private val DEFAULT_MINI_MESSAGE = MiniMessage.miniMessage()

        fun of(value: String): MiniString = MiniString(value)
    }

    fun with(resolver: TagResolver?): MiniString {
        val temp = ArrayList(placeholders)
        temp.add(resolver)
        return MiniString(value, miniMessage, temp)
    }

    fun with(templates: Collection<TagResolver>): MiniString {
        if (templates.isEmpty()) return this
        val temp = ArrayList(placeholders)
        temp.addAll(templates)
        return MiniString(value, miniMessage, temp)
    }

    fun with(vararg resolvers: TagResolver): MiniString {
        if (resolvers.isEmpty()) return this
        return with(resolvers.toList())
    }

    fun with(key: String, value: String): MiniString {
        return with(Placeholder.component(key, Component.text(value)))
    }

    fun with(key: String, value: Component): MiniString {
        return with(Placeholder.component(key, value))
    }

    fun with(key: String, value: ComponentLike): MiniString {
        return with(key, value.asComponent())
    }

    override fun asComponent(): Component {
        return miniMessage.deserialize(value, TagResolver.resolver(placeholders))
    }
}