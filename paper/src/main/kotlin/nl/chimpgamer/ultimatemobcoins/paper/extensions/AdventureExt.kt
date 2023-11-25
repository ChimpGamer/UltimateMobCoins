package nl.chimpgamer.ultimatemobcoins.paper.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

private val legacyComponentSerializer = LegacyComponentSerializer.builder().character('&').hexColors().build()

fun String.toComponent() = Component.text(this)

fun String.parseLegacy() = legacyComponentSerializer.deserialize(this)