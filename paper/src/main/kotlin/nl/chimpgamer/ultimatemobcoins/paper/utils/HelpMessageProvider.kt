package nl.chimpgamer.ultimatemobcoins.paper.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MessagesConfig
import org.bukkit.command.CommandSender
import org.incendo.cloud.minecraft.extras.MinecraftHelp

class HelpMessageProvider(messagesConfig: MessagesConfig) : MinecraftHelp.MessageProvider<CommandSender> {
    private val title = MiniString.of(messagesConfig.commandHelpTitle)
    private val command = MiniString.of(messagesConfig.commandHelpCommand)
    private val description = MiniString.of(messagesConfig.commandHelpDescription)
    private val noDescription = MiniString.of(messagesConfig.commandHelpNoDescription)
    private val arguments = MiniString.of(messagesConfig.commandHelpArguments)
    private val optional = MiniString.of(messagesConfig.commandHelpOptional)
    private val searchResults = MiniString.of(messagesConfig.commandHelpShowingResultsForQuery)
    private val noResults = MiniString.of(messagesConfig.commandHelpNoResultsForQuery)
    private val availableCommands = MiniString.of(messagesConfig.commandHelpAvailableCommands)
    private val clickToShowHelp = MiniString.of(messagesConfig.commandHelpClickToShopHelp)
    private val pageOutOfRange = MiniString.of(messagesConfig.commandHelpPageOutOfRange)
    private val clickForNextPage = MiniString.of(messagesConfig.commandHelpClickForNextPage)
    private val clickForPreviousPage = MiniString.of(messagesConfig.commandHelpClickForPreviousPage)

    override fun provide(
        sender: CommandSender,
        key: String,
        args: MutableMap<String?, String?>
    ): Component {
        return (when (key) {
            MinecraftHelp.MESSAGE_HELP_TITLE -> this.title
            MinecraftHelp.MESSAGE_COMMAND -> this.command
            MinecraftHelp.MESSAGE_DESCRIPTION -> this.description
            MinecraftHelp.MESSAGE_NO_DESCRIPTION -> this.noDescription
            MinecraftHelp.MESSAGE_ARGUMENTS -> this.arguments
            MinecraftHelp.MESSAGE_OPTIONAL -> this.optional
            MinecraftHelp.MESSAGE_SHOWING_RESULTS_FOR_QUERY -> this.searchResults
            MinecraftHelp.MESSAGE_NO_RESULTS_FOR_QUERY -> this.noResults
            MinecraftHelp.MESSAGE_AVAILABLE_COMMANDS -> this.availableCommands
            MinecraftHelp.MESSAGE_PAGE_OUT_OF_RANGE -> this.pageOutOfRange
            MinecraftHelp.MESSAGE_CLICK_TO_SHOW_HELP -> this.clickToShowHelp
            MinecraftHelp.MESSAGE_CLICK_FOR_NEXT_PAGE -> this.clickForNextPage
            MinecraftHelp.MESSAGE_CLICK_FOR_PREVIOUS_PAGE -> this.clickForPreviousPage
            else -> throw IllegalArgumentException("Unknown message")
        }).with(object : TagResolver.WithoutArguments {
            @Throws(ParsingException::class)
            override fun resolve(name: String): Tag? {
                val arg = args[name]
                if (arg != null) return Tag.preProcessParsed(arg)
                return null
            }
        }).asComponent()
    }
}