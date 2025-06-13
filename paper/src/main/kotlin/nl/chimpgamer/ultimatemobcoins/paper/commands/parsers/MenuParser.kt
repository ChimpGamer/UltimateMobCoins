package nl.chimpgamer.ultimatemobcoins.paper.commands.parsers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.commands.captions.UltimateMobCoinsCaptionKeys
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.Menu
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.caption.CaptionVariable
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion
import java.util.Objects

class MenuParser<C> : ArgumentParser<C, Menu>, BlockingSuggestionProvider<C> {
    private val plugin by lazy { JavaPlugin.getPlugin(UltimateMobCoinsPlugin::class.java) }

    companion object {
        fun <C> menuParser(): ParserDescriptor<C, Menu> {
            return ParserDescriptor.of(MenuParser(), Menu::class.java)
        }

        fun <C> menuComponent(): CommandComponent.Builder<C, Menu> {
            return CommandComponent.builder<C, Menu>().parser(menuParser())
        }
    }

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput
    ): ArgumentParseResult<Menu> {
        val input = commandInput.peekString()

        val menu = plugin.shopMenus[input]
        commandInput.readString()
        return if (menu == null) ArgumentParseResult.failure(NMExtensionParseException(input, commandContext)) else ArgumentParseResult.success(menu)
    }

    override fun suggestions(context: CommandContext<C>, input: CommandInput): Iterable<Suggestion> {
        return plugin.shopMenus.keys.map { Suggestion.suggestion(it) }.toList()
    }

    class NMExtensionParseException(val input: String, context: CommandContext<*>) : ParserException(
        MenuParser::class.java,
        context,
        UltimateMobCoinsCaptionKeys.ARGUMENT_PARSE_FAILURE_MENU,
        CaptionVariable.of("input", input)
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            } else if (other != null && this.javaClass == other.javaClass) {
                val that = other as NMExtensionParseException
                return this.input == that.input
            } else {
                return false
            }
        }

        override fun hashCode(): Int {
            return Objects.hash(*arrayOf(this.input))
        }
    }
}
