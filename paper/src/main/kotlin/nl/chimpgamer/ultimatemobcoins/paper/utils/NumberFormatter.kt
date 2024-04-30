package nl.chimpgamer.ultimatemobcoins.paper.utils

import java.math.BigDecimal
import java.text.DecimalFormat

object NumberFormatter {
    val COMMAS_FORMAT: DecimalFormat = DecimalFormat("#,###")
    val FIXED_FORMAT: DecimalFormat = DecimalFormat("#")
    private var PRETTY_FORMAT: DecimalFormat = DecimalFormat("###,##0.00")

    internal fun setPrettyFormat(pattern: String) {
        PRETTY_FORMAT = DecimalFormat(pattern)
    }

    fun displayCurrency(value: BigDecimal?): String {
        var str = PRETTY_FORMAT.format(value)
        if (str.endsWith(".00")) {
            str = str.substring(0, str.length - 3)
        }
        return str
    }
}