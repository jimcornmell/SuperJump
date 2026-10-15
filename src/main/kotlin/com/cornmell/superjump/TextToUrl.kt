package com.cornmell.superjump

import com.cornmell.superjump.model.Jumpers

class TextToUrl {
    fun toUrl(
        filename: String,
        lineNumber: Int,
        columnNumber: Int,
        lineText: String,
    ): String? {
        val word = findCurrentWord(lineText, lineNumber, columnNumber)

        if (word?.isEmpty() == true) {
            return ""
        }

        for ((enabled, _, regex, url, fileRegex) in Jumpers.all) {
            if (!enabled) {
                continue
            }

            if (fileRegex != null &&
                !fileRegex.matches(filename)
            ) {
                continue
            }

            // Try the current word first.
            if (word != null) {
                val match = regex.find(word)

                if (match != null) {
                    return expandUrl(url, match)
                }
            }

            // Then try the whole line.
            val match = regex.find(lineText)

            if (match != null) {
                return expandUrl(url, match)
            }
        }

        return null
    }

    private fun expandUrl(
        template: String,
        match: MatchResult,
    ): String {
        var url = template

        for (i in 1..match.groupValues.lastIndex) {
            url = url.replace($$"$$$i", match.groupValues[i])
        }

        return url
    }

    private fun findCurrentWord(
        lineText: String,
        lineNumber: Int,
        columnNumber: Int,
    ): String? {
        if (lineText.isEmpty() || columnNumber !in lineText.indices) {
            return null
        }

        if (isWordDelimiter(lineText[columnNumber])) {
            return null
        }

        var start = columnNumber
        while (start > 0 && !isWordDelimiter(lineText[start - 1])) {
            start--
        }

        var end = columnNumber
        while (end < lineText.length && !isWordDelimiter(lineText[end])) {
            end++
        }

        return lineText.substring(start, end)
    }

    private fun isWordDelimiter(char: Char): Boolean =
        char.isWhitespace() ||
                char in "()[]{}<>,;:\"'`"
}
