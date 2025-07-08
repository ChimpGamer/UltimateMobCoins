package nl.chimpgamer.ultimatemobcoins.paper.managers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.commands.MobCoinsCommand
import nl.chimpgamer.ultimatemobcoins.paper.commands.captions.UltimateMobCoinsCaptionKeys
import nl.chimpgamer.ultimatemobcoins.paper.utils.HelpMessageProvider
import org.bukkit.command.CommandSender
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.caption.CaptionProvider
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.AudienceProvider
import org.incendo.cloud.minecraft.extras.ImmutableMinecraftHelp
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter
import org.incendo.cloud.paper.LegacyPaperCommandManager
import java.util.logging.Level

class CloudCommandManager(private val plugin: UltimateMobCoinsPlugin) {

    private lateinit var paperCommandManager: LegacyPaperCommandManager<CommandSender>
    lateinit var mobCoinHelp: MinecraftHelp<CommandSender>

    fun initialize() {
        try {
            paperCommandManager = LegacyPaperCommandManager.createNative(plugin, ExecutionCoordinator.asyncCoordinator())

            if (paperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                paperCommandManager.registerBrigadier()
                val brigadierManager = paperCommandManager.brigadierManager()
                brigadierManager.setNativeNumberSuggestions(false)
            } else if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                paperCommandManager.registerAsynchronousCompletions()
            }

            paperCommandManager.captionRegistry().apply {
                registerProvider(CaptionProvider.constantProvider(StandardCaptionKeys.EXCEPTION_NO_PERMISSION, plugin.messagesConfig.noPermission))
                registerProvider(CaptionProvider.forCaption(UltimateMobCoinsCaptionKeys.ARGUMENT_PARSE_FAILURE_MENU) { sender -> "Menu '{input}' does not exist!" })
                /*registerProvider(CaptionProvider.forCaption(Caption.of("help.minecraft.help")) { sender -> plugin.messagesConfig.commandHelpTitle } )
                registerProvider(CaptionProvider.forCaption(Caption.of("help.minecraft.showing_results_for_query")) { sender -> plugin.messagesConfig.commandHelpShowingResultsForQuery } )
                registerProvider(CaptionProvider.forCaption(Caption.of("help.minecraft.no_results_for_query")) { sender -> plugin.messagesConfig.commandHelpNoResultsForQuery } )
                registerProvider(CaptionProvider.forCaption(Caption.of("help.minecraft.available_commands")) { sender -> plugin.messagesConfig.commandHelpAvailableCommands } )*/
            }

            MinecraftExceptionHandler.createNative<CommandSender>()
                .defaultHandlers()
                .captionFormatter(ComponentCaptionFormatter.miniMessage())
                .registerTo(paperCommandManager)

            val name = plugin.settingsConfig.commandName
            mobCoinHelp = ImmutableMinecraftHelp.builder<CommandSender>()
                .commandManager(paperCommandManager)
                .audienceProvider(AudienceProvider.nativeAudience())
                .commandPrefix("/$name help")
                .messageProvider(HelpMessageProvider(plugin.messagesConfig))
                .build()
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to initialize the command manager", ex)
        }
    }

    fun loadCommands() {
        val name = plugin.settingsConfig.commandName
        val aliases = plugin.settingsConfig.commandAliases
        MobCoinsCommand(plugin).registerCommands(paperCommandManager, name, *aliases.toTypedArray())
    }
}