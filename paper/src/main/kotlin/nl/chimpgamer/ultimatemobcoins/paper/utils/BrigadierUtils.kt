package nl.chimpgamer.ultimatemobcoins.paper.utils

/*
Code taken from Brigadier's StringArgumentType and StringReader
*/
object BrigadierUtils {
    private fun isAllowedInUnquotedString(c: Char): Boolean {
        return c in '0'..'9' || c in 'A'..'Z' || c in 'a'..'z' || c == '_' || c == '-' || c == '.' || c == '+'
    }

    fun escapeIfRequired(input: String, quoted: Boolean): String {
        if (quoted) {
            return escape(input)
        }

        for (c in input.toCharArray()) {
            if (!isAllowedInUnquotedString(c)) {
                return "\"" + input + "\""
            }
        }
        return input
    }

    private fun escape(input: String): String {
        val result = StringBuilder("\"")

        for (element in input) {
            if (element == '\\' || element == '"') {
                result.append('\\')
            }
            result.append(element)
        }

        result.append("\"")
        return result.toString()
    }
}