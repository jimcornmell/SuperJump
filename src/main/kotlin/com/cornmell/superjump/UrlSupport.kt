package com.cornmell.superjump

import java.net.URI

internal object UrlSupport {
    private val placeholder = Regex("""\$(\d+)""")
    private const val SAFE = "-._~:/?#[]@!$&'()*+,;=%"

    /** Only http(s) URLs with a host may be opened in the browser. */
    fun isBrowsable(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        return uri.scheme?.lowercase() in listOf("http", "https") && !uri.host.isNullOrEmpty()
    }

    /** Replaces $n placeholders in a single pass, percent-encoding unsafe characters of captured text. */
    fun expand(template: String, match: MatchResult): String =
        placeholder.replace(template) { token ->
            val index = token.groupValues[1].toIntOrNull()
            if (index == null || index > match.groupValues.lastIndex) {
                token.value
            } else {
                encodeUnsafe(match.groupValues[index])
            }
        }

    private fun encodeUnsafe(value: String): String {
        val out = StringBuilder()
        for (byte in value.toByteArray(Charsets.UTF_8)) {
            val c = (byte.toInt() and 0xFF).toChar()
            if (byte >= 0 && (c.isLetterOrDigit() || c in SAFE)) {
                out.append(c)
            } else {
                out.append('%').append("%02X".format(byte.toInt() and 0xFF))
            }
        }
        return out.toString()
    }
}
