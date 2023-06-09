package nl.chimpgamer.ultimatemobcoins.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.DoubleArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument
import de.tr7zw.nbtapi.NBTItem
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.toComponent
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.MenuType
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.SpinnerPrizesMenu
import nl.chimpgamer.ultimatemobcoins.paper.utils.LogWriter
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.MathContext

class MobCoinsCommand(private val plugin: UltimateMobCoinsPlugin) {
    private val spinnerPrizesMenu = SpinnerPrizesMenu(plugin)

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatemobcoins.command.mobcoins"

        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        val offlinePlayerArgument = OfflinePlayerArgument.of<CommandSender>("player")
        val amountArgument = DoubleArgument.of<CommandSender>("amount")

        val shopArgument = StringArgument.builder<CommandSender>("shop")
            .asOptionalWithDefault(plugin.settingsConfig.commandDefaultShop)
            .withSuggestionsProvider { _, _ -> plugin.shopMenus.keys.toList() }.build()

        commandManager.command(builder
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender as Player
                val user = plugin.userManager.getByUUID(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@handler
                }
                val replacements = mapOf(
                    "coins" to user.coinsAsDouble,
                    "coins_collected" to user.coinsCollectedAsDouble,
                    "coins_spent" to user.coinsSpentAsDouble
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalance.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("help")
            .permission("$basePermission.help")
            .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
            .handler { context ->
                plugin.cloudCommandManager.mobCoinHelp.queryCommands(
                    context.getOrDefault("query", ""),
                    context.sender
                )
            }
        )

        commandManager.command(builder
            .literal("reload")
            .permission("$basePermission.reload")
            .handler { context ->
                val sender = context.sender
                plugin.reload()
                sender.sendRichMessage("<green>Successfully reloaded configs!")
            }
        )

        commandManager.command(builder
            .literal("refresh")
            .permission("$basePermission.refresh")
            .handler { context ->
                val sender = context.sender
                plugin.shopMenus.values.filter { it.menuType === MenuType.ROTATING_SHOP }
                    .forEach { it.refreshShopItems() }
                sender.sendRichMessage("<green>Successfully refreshed rotating shops!")
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("shop")
            .argument(shopArgument.copy())
            .handler { context ->
                val sender = context.sender as Player
                val shopName = context[shopArgument]
                plugin.shopMenus[shopName]?.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("spinnerprizes")
            .permission("$basePermission.spinnerprizes")
            .handler { context ->
                val sender = context.sender as Player
                spinnerPrizesMenu.inventory.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("spinner")
            .permission("$basePermission.spinner")
            .handler { context ->
                val sender = context.sender as Player
                val user = plugin.userManager.getByUUID(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@handler
                }
                val usageCosts = plugin.spinnerManager.usageCosts
                if (user.coins >= usageCosts.toBigDecimal()) {
                    user.withdrawCoins(usageCosts)
                    user.addCoinsSpent(usageCosts)
                    if (plugin.settingsConfig.logSpinner) {
                        LogWriter(plugin, "${sender.name} payed $usageCosts mobcoins to spin the spinner.").run() // Just run because commands are async
                    }
                } else {
                    sender.sendRichMessage(plugin.messagesConfig.spinnerNotEnoughMobCoins)
                    return@handler
                }

                plugin.spinnerManager.spinnerMenu.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("balance")
            .permission("$basePermission.balance")
            .handler { context ->
                val sender = context.sender as Player
                val user = plugin.userManager.getByUUID(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@handler
                }
                val replacements = mapOf(
                    "coins" to user.coinsAsDouble,
                    "coins_collected" to user.coinsCollectedAsDouble,
                    "coins_spent" to user.coinsSpentAsDouble
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalance.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("balance")
            .permission("$basePermission.balance.others")
            .argument(offlinePlayerArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val displayName =
                    targetPlayer.player?.displayName() ?: targetPlayer.name?.toComponent() ?: return@handler
                val replacements = mapOf(
                    "displayname" to displayName,
                    "coins" to user.coinsAsDouble,
                    "coins_collected" to user.coinsCollectedAsDouble,
                    "coins_spent" to user.coinsSpentAsDouble
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalanceOthers.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("set")
            .permission("$basePermission.set")
            .argument(offlinePlayerArgument.copy())
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                user.coins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (if (sender is Player) sender.displayName() else sender.name()),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsSetSender.parse(replacements))
                targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsSetTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("give")
            .permission("$basePermission.give")
            .argument(offlinePlayerArgument.copy())
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                user.depositCoins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (if (sender is Player) sender.displayName() else sender.name()),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsGiveSender.parse(replacements))
                targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsGiveTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("take")
            .permission("$basePermission.take")
            .argument(offlinePlayerArgument.copy())
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                user.withdrawCoins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (if (sender is Player) sender.displayName() else sender.name()),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsTakeSender.parse(replacements))
                targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsTakeTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("pay")
            .permission("$basePermission.pay")
            .argument(offlinePlayerArgument.copy())
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender as Player
                val targetPlayer = context[offlinePlayerArgument]

                if (sender == targetPlayer) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsCannotPayYourself.parse())
                    return@handler
                }

                val user = plugin.userManager.getByUUID(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                if (user.coins < amount.toBigDecimal()) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsNotEnough.parse(mapOf("amount" to amount)))
                    return@handler
                }

                val targetUser = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (targetUser == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                user.withdrawCoins(amount)
                targetUser.depositCoins(amount)
                sender.sendMessage(
                    plugin.messagesConfig.mobCoinsPaySender.parse(
                        mapOf(
                            "amount" to amount,
                            "displayname" to (targetPlayer.player?.displayName() ?: targetPlayer.name?.toComponent())
                        )
                    )
                )
                targetPlayer.player?.sendMessage(
                    plugin.messagesConfig.mobCoinsPayTarget.parse(
                        mapOf(
                            "amount" to amount,
                            "displayname" to sender.displayName()
                        )
                    )
                )
                if (plugin.settingsConfig.logPay) {
                    LogWriter(plugin, "${sender.name} paid ${targetPlayer.name} $amount mobcoins").runAsync()
                }
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("withdraw")
            .permission("$basePermission.withdraw")
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender as Player
                val user = plugin.userManager.getByUUID(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                if (user.coins < amount.toBigDecimal()) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsNotEnough.parse(mapOf("amount" to amount)))
                    return@handler
                }
                if (sender.inventory.firstEmpty() == -1) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsInventoryFull.parse())
                    return@handler
                }
                val finalAmount = amount.toBigDecimal(MathContext(3))
                user.withdrawCoins(finalAmount)

                val amountPlaceholder = Placeholder.unparsed("amount", finalAmount.toString())
                val mobCoinItem = plugin.settingsConfig.getMobCoinsItem(amountPlaceholder)
                val nbtMobCoin = NBTItem(mobCoinItem)
                nbtMobCoin.setBoolean("isMobCoin", true)
                nbtMobCoin.setDouble("amount", finalAmount.toDouble())

                sender.inventory.addItem(nbtMobCoin.item)
                sender.sendMessage(plugin.messagesConfig.mobCoinsWithdraw.parse(amountPlaceholder))
                if (plugin.settingsConfig.logWithdraw) {
                    LogWriter(
                        plugin,
                        "${sender.name} withdrew $amount mobcoins (${user.coins} mobcoins)"
                    ).runAsync()
                }
            }
        )
    }
}