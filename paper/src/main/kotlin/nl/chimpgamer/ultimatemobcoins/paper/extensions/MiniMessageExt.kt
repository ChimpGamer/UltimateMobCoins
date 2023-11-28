package nl.chimpgamer.ultimatemobcoins.paper.extensions

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

val newlineSplitRegex = "(<br>|<newline>)".toRegex()
private val miniMessageTagRegex = Regex("<[!?#]?[a-z0-9_-]*>")

fun String.parse() = miniMessage().deserialize(this).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

fun String.parse(tagResolver: TagResolver) = miniMessage().deserialize(this, tagResolver).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
fun String.parse(player: Player?, tagResolver: TagResolver) = miniMessage().deserialize(this, TagResolver.resolver(
    placeholderAPIPlaceholdersToTagResolver(player), tagResolver)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

fun String.parse(replacements: Map<String, *>) = parse(replacements.toTagResolver())
fun String.parse(player: Player?, replacements: Map<String, *>) = parse(player, replacements.toTagResolver())

fun String.isValid(): Boolean = miniMessage().deserializeOrNull(this) != null

fun String.containsMiniMessage(): Boolean = miniMessageTagRegex.containsMatchIn(this)

fun Map<String, *>.toTagResolver(parsed: Boolean = false) = TagResolver.resolver(
    map { (key, value) ->
        if (value is ComponentLike) Placeholder.component(key, value)
        else if (parsed) Placeholder.parsed(key, value.toString())
        else Placeholder.unparsed(key, value.toString())
    }
)

internal fun placeholderAPIPlaceholdersToTagResolver(player: Player?): TagResolver {
    return TagResolver.resolver(
        "papi"
    ) { argumentQueue: ArgumentQueue, _: Context? ->
        // Check if papi is available, and we are on the correct platform.
        if (!isPlaceholderAPI) return@resolver null

        // Get the string placeholder that they want to use.
        val papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value()

        // Then get PAPI to parse the placeholder for the given player.
        val parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, "%$papiPlaceholder%")

        if (parsedPlaceholder.containsMiniMessage()) {
            Tag.preProcessParsed(parsedPlaceholder)
        } else {
            // We need to turn this ugly legacy string into a nice component.
            val componentPlaceholder = parsedPlaceholder.parseLegacy()
            Tag.selfClosingInserting(componentPlaceholder)
        }
    }
}

private val isPlaceholderAPI: Boolean = try {
    Class.forName("me.clip.placeholderapi.PlaceholderAPI")
    true
} catch (ex: ClassNotFoundException) {
    false
}