package nl.chimpgamer.ultimatemobcoins.paper.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.*
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.MenuType
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.SpinnerPrizesMenu
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser.offlinePlayerParser
import org.incendo.cloud.bukkit.parser.PlayerParser.playerParser
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.DoubleParser.doubleParser
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.SuggestionProvider
import java.math.MathContext

class MobCoinsCommand(private val plugin: UltimateMobCoinsPlugin) {
    private val spinnerPrizesMenu = SpinnerPrizesMenu(plugin)

    private val paginationBuilder = Pagination.builder()
        .width(53)
        .resultsPerPage(17)
        .renderer(object : Pagination.Renderer {
            override fun renderEmpty(): Component {
                return "<gray>There are no entries!".parse()
            }
        })

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatemobcoins.command.mobcoins"

        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        val playerKey = CloudKey.of("player", Player::class.java)
        val offlinePlayerKey = CloudKey.of("offlineplayer", OfflinePlayer::class.java)
        val amountKey = CloudKey.of("amount", Double::class.java)
        val pageKey = CloudKey.of("page", Int::class.java)

        val silentFlag = commandManager.flagBuilder("silent").withAliases("s").build()

        val shopArgument = CommandComponent.builder<Player, String>()
            .name("shop")
            .optional(DefaultValue.constant(plugin.settingsConfig.commandDefaultShop))
            .suggestionProvider(SuggestionProvider.suggestingStrings(plugin.shopMenus.keys))
            .parser(stringParser())
            .build()

        commandManager.command(builder
            .senderType(Player::class.java)
            .suspendingHandler { context ->
                val sender = context.sender()
                val user = plugin.userManager.getUser(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@suspendingHandler
                }
                val replacements = mapOf(
                    "coins" to user.coinsPretty,
                    "coins_collected" to user.coinsCollectedPretty,
                    "coins_spent" to user.coinsSpentPretty
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalance.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("help")
            .permission("$basePermission.help")
            .optional("query", greedyStringParser(), DefaultValue.constant(""))
            .handler { context ->
                plugin.cloudCommandManager.mobCoinHelp.queryCommands(context.get("query"), context.sender())
            }
        )

        commandManager.command(builder
            .literal("reload")
            .flag(commandManager.flagBuilder("menus").withAliases("m"))
            .permission("$basePermission.reload")
            .handler { context ->
                val sender = context.sender()
                val reloadMenus = context.flags().contains("menus")

                plugin.reload()
                if (reloadMenus) {
                    plugin.reloadMenus()
                }
                sender.sendRichMessage("<green>Successfully reloaded configs${if (reloadMenus) " and menus" else ""}!")
            }
        )

        commandManager.command(builder
            .literal("refresh")
            .permission("$basePermission.refresh")
            .handler { context ->
                val sender = context.sender()
                plugin.shopMenus.values.filter { it.menuType === MenuType.ROTATING_SHOP }
                    .forEach { it.refreshShopItems() }
                sender.sendRichMessage("<green>Successfully refreshed rotating shops!")
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("shop")
            .argument(shopArgument)
            .handler { context ->
                val sender = context.sender()
                val shopName = context[shopArgument]
                plugin.shopMenus[shopName]?.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("spinnerprizes")
            .permission("$basePermission.spinnerprizes")
            .handler { context ->
                val sender = context.sender()
                spinnerPrizesMenu.inventory.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("spinner")
            .permission("$basePermission.spinner")
            .suspendingHandler { context ->
                val sender = context.sender()
                val user = plugin.userManager.getUser(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@suspendingHandler
                }
                val usageCosts = plugin.spinnerManager.usageCosts
                if (user.coins >= usageCosts.toBigDecimal()) {
                    user.withdrawCoins(usageCosts)
                    user.addCoinsSpent(usageCosts)
                    if (plugin.settingsConfig.logSpinner) {
                        plugin.logWriter.write("${sender.name} payed $usageCosts mobcoins to spin the spinner.")
                    }

                    plugin.spinnerManager.spinnerMenu.open(sender)
                } else {
                    sender.sendRichMessage(plugin.messagesConfig.spinnerNotEnoughMobCoins)
                }
            }
        )

        commandManager.command(builder
            .literal("spinner")
            .permission("$basePermission.spinner.others")
            .required(playerKey, playerParser())
            .suspendingHandler { context ->
                val targetPlayer = context[playerKey]
                val user = plugin.userManager.getUser(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@suspendingHandler
                }
                val usageCosts = plugin.spinnerManager.usageCosts
                if (user.coins >= usageCosts.toBigDecimal()) {
                    user.withdrawCoins(usageCosts)
                    user.addCoinsSpent(usageCosts)
                    if (plugin.settingsConfig.logSpinner) {
                        plugin.logWriter.write("${targetPlayer.name} payed $usageCosts mobcoins to spin the spinner.")
                    }

                    plugin.spinnerManager.spinnerMenu.open(targetPlayer)
                } else {
                    targetPlayer.sendRichMessage(plugin.messagesConfig.spinnerNotEnoughMobCoins)
                }
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("balance")
            .permission("$basePermission.balance")
            .suspendingHandler { context ->
                val sender = context.sender()
                val user = plugin.userManager.getUser(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@suspendingHandler
                }
                val replacements = mapOf(
                    "coins" to user.coinsPretty,
                    "coins_collected" to user.coinsCollectedPretty,
                    "coins_spent" to user.coinsSpentPretty
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalance.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("balance")
            .permission("$basePermission.balance.others")
            .required(offlinePlayerKey, offlinePlayerParser())
            .suspendingHandler { context ->
                val sender = context.sender()
                val targetPlayer = context[offlinePlayerKey]
                val user = plugin.userManager.getUser(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@suspendingHandler
                }
                val replacements = mapOf(
                    "displayname" to (targetPlayer.player?.displayName() ?: targetPlayer.name),
                    "coins" to user.coinsPretty,
                    "coins_collected" to user.coinsCollectedPretty,
                    "coins_spent" to user.coinsSpentPretty
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalanceOthers.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("set")
            .permission("$basePermission.set")
            .required(offlinePlayerKey, offlinePlayerParser())
            .required(amountKey, doubleParser(0.0))
            .flag(silentFlag)
            .suspendingHandler { context ->
                val sender = context.sender()
                val targetPlayer = context[offlinePlayerKey]
                val amount = context[amountKey]
                val isSilent = context.flags().isPresent(silentFlag)

                val user = plugin.userManager.getUser(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@suspendingHandler
                }
                user.coins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (targetPlayer.player?.displayName() ?: targetPlayer.name),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsSetSender.parse(replacements))
                if (!isSilent)
                    targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsSetTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("give")
            .permission("$basePermission.give")
            .required(offlinePlayerKey, offlinePlayerParser())
            .required(amountKey, doubleParser(0.1))
            .flag(silentFlag)
            .suspendingHandler { context ->
                val sender = context.sender()
                val targetPlayer = context[offlinePlayerKey]
                val amount = context[amountKey]
                val isSilent = context.flags().isPresent(silentFlag)

                val user = plugin.userManager.getUser(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@suspendingHandler
                }
                user.depositCoins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (targetPlayer.player?.displayName() ?: targetPlayer.name),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsGiveSender.parse(replacements))
                if (!isSilent)
                    targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsGiveTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("take")
            .permission("$basePermission.take")
            .required(offlinePlayerKey, offlinePlayerParser())
            .required(amountKey, doubleParser(0.1))
            .flag(silentFlag)
            .suspendingHandler { context ->
                val sender = context.sender()
                val targetPlayer = context[offlinePlayerKey]
                val amount = context[amountKey]
                val isSilent = context.flags().isPresent(silentFlag)

                val user = plugin.userManager.getUser(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@suspendingHandler
                }
                user.withdrawCoins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (targetPlayer.player?.displayName() ?: targetPlayer.name),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsTakeSender.parse(replacements))
                if (!isSilent)
                    targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsTakeTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("pay")
            .permission("$basePermission.pay")
            .required(offlinePlayerKey, offlinePlayerParser())
            .required(amountKey, doubleParser(0.1))
            .suspendingHandler { context ->
                val sender = context.sender()
                val targetPlayer = context[offlinePlayerKey]

                if (sender == targetPlayer) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsCannotPayYourself.parse())
                    return@suspendingHandler
                }

                val user = plugin.userManager.getUser(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@suspendingHandler
                }
                val amount = context[amountKey]
                if (user.coins < amount.toBigDecimal()) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsNotEnough.parse(mapOf("amount" to amount)))
                    return@suspendingHandler
                }

                val targetUser = plugin.userManager.getUser(targetPlayer.uniqueId)
                if (targetUser == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@suspendingHandler
                }
                user.withdrawCoins(amount)
                targetUser.depositCoins(amount)
                sender.sendMessage(
                    plugin.messagesConfig.mobCoinsPaySender.parse(
                        mapOf(
                            "amount" to amount,
                            "displayname" to (targetPlayer.player?.displayName()
                                ?: targetPlayer.name?.toComponent())
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
                    plugin.logWriter.write("${sender.name} paid ${targetPlayer.name} $amount mobcoins")
                }
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("withdraw")
            .permission("$basePermission.withdraw")
            .required(amountKey, doubleParser(0.1))
            .suspendingHandler { context ->
                val sender = context.sender()
                val user = plugin.userManager.getUser(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@suspendingHandler
                }
                val amount = context[amountKey]
                if (user.coins < amount.toBigDecimal()) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsNotEnough.parse(mapOf("amount" to amount)))
                    return@suspendingHandler
                }
                if (sender.inventory.firstEmpty() == -1) {
                    sender.sendMessage(plugin.messagesConfig.mobCoinsInventoryFull.parse())
                    return@suspendingHandler
                }
                val finalAmount = amount.toBigDecimal(MathContext(3))
                user.withdrawCoins(finalAmount)
                val amountAsDouble = finalAmount.toDouble()

                val amountPlaceholder = Placeholder.unparsed("amount", amountAsDouble.toString())
                val mobCoinItem = plugin.settingsConfig.getMobCoinsItem(amountPlaceholder)
                mobCoinItem.editMeta { meta ->
                    meta.pdc {
                        setBoolean(NamespacedKeys.isMobCoin, true)
                        setDouble(NamespacedKeys.mobCoinAmount, amountAsDouble)
                    }
                }

                sender.inventory.addItem(mobCoinItem)
                sender.sendMessage(plugin.messagesConfig.mobCoinsWithdraw.parse(amountPlaceholder))
                if (plugin.settingsConfig.logWithdraw) {
                    plugin.logWriter.write("${sender.name} withdrew $amountAsDouble mobcoins (${user.coins} mobcoins)")
                }
            }
        )

        commandManager.command(builder
            .literal("top")
            .permission("$basePermission.top")
            .optional(pageKey, integerParser(1), DefaultValue.constant(1))
            .suspendingHandler { context ->
                val sender = context.sender()
                val page = context[pageKey]
                val rows = ArrayList<Component>()
                plugin.userManager.getTopMobCoins().forEach { user ->
                    rows.add(plugin.messagesConfig.mobCoinsTopEntry.parse(mapOf("player_name" to user.username, "mobcoins" to NumberFormatter.displayCurrency(user.coins))))
                }
                val render = paginationBuilder.build(
                    plugin.messagesConfig.mobCoinsTopTitle.parse(), { value: Component?, index: Int ->
                        listOf(
                            value?.replaceText { it.once().matchLiteral("<position>").replacement(Component.text(index + 1)).build() }
                                ?: "<green>${index + 1}. <red>ERR?".parse()
                        )
                    }, { otherPage -> "/$name top $otherPage" }
                ).render(rows, page)
                render.forEach(sender::sendMessage)
            }
        )

        commandManager.command(builder
            .literal("grindtop")
            .permission("$basePermission.grindtop")
            .optional(pageKey, integerParser(1), DefaultValue.constant(1))
            .suspendingHandler { context ->
                val sender = context.sender()
                val page = context[pageKey]
                val rows = ArrayList<Component>()
                plugin.userManager.getGrindTop().forEach { user ->
                    rows.add(plugin.messagesConfig.mobCoinsGrindTopEntry.parse(mapOf("player_name" to user.username, "mobcoins" to NumberFormatter.displayCurrency(user.coinsCollected))))
                }
                val render = paginationBuilder.build(
                    plugin.messagesConfig.mobCoinsGrindTopTitle.parse(), { value: Component?, index: Int ->
                        listOf(
                            value?.replaceText { it.once().matchLiteral("<position>").replacement(Component.text(index + 1)).build() }
                                ?: "<green>${index + 1}. <red>ERR?".parse()
                        )
                    }, { otherPage -> "/$name grindtop $otherPage" }
                ).render(rows, page)
                render.forEach(sender::sendMessage)
            }
        )
    }
}