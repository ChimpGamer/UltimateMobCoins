package nl.chimpgamer.ultimatemobcoins.paper.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object NumberFormatter {
    val COMMAS_FORMAT: DecimalFormat = DecimalFormat("#,###")
    val FIXED_FORMAT: DecimalFormat = DecimalFormat("#")
    private var PRETTY_FORMAT: DecimalFormat = DecimalFormat("###,##0.00")
    val compactSuffixes = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')

    internal fun setPrettyFormat(pattern: String, symbolLocaleString: String?) {
        val decimalFormatSymbols = if (symbolLocaleString != null) {
            DecimalFormatSymbols.getInstance(Locale.forLanguageTag(symbolLocaleString))
        } else {
            // Fallback to the JVM's default locale
            DecimalFormatSymbols.getInstance(Locale.US)
        }

        val currencyFormat = DecimalFormat(pattern, decimalFormatSymbols)
        currencyFormat.roundingMode = RoundingMode.FLOOR

        PRETTY_FORMAT = currencyFormat
    }

    fun displayCurrency(value: BigDecimal?): String {
        var str = PRETTY_FORMAT.format(value)
        if (str.endsWith(".00")) {
            str = str.substring(0, str.length - 3)
        }
        return str
    }

    fun compactDecimalFormat(number: Number): String {
        val numValue = number.toLong()
        val value = floor(log10(numValue.toDouble())).toInt()
        val base = value / 3

        return if (value >= 3 && base < compactSuffixes.size) {
            DecimalFormat("#0.0").format(
                numValue / 10.0.pow((base * 3).toDouble())
            ) + compactSuffixes[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }
}