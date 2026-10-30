package com.cornmell.superjump

import com.cornmell.superjump.model.Jumpers

class TextToUrl {
    fun toUrl(
        filename: String,
        lineNumber: Int,
        columnNumber: Int,
        lineText: String,
        documentText: CharSequence? = null,
    ): String? {
        val codeBlockLanguage = documentText
            ?.takeIf { filename.endsWith(".md", true) || filename.endsWith(".markdown", true) }
            ?.let { findCodeBlockLanguage(it, lineNumber) }
        val word = findCurrentWord(lineText, lineNumber, columnNumber)

        val deadline = System.nanoTime() + REGEX_BUDGET_NANOS

        for (jumper in Jumpers.all) {
            if (!jumper.enabled) {
                continue
            }

            val fileRegex = jumper.fileRegex

            if (fileRegex != null &&
                !fileRegex.matches(filename) &&
                !(codeBlockLanguage != null && jumper.fileRegexIgnoreCase?.matches(codeBlockLanguage) == true)
            ) {
                continue
            }

            try {
                // Try the current word first, then the whole line.
                val match = word?.let { jumper.regex.find(Bounded(it, deadline)) }
                    ?: jumper.regex.find(Bounded(lineText, deadline))

                if (match != null) {
                    return UrlSupport.expand(jumper.url, match)
                }
            } catch (_: RegexTimeoutException) {
                // A pathological user regex must not freeze the editor; skip it.
                return null
            }
        }

        return null
    }

    private class RegexTimeoutException : RuntimeException()

    /** Aborts regex matching once the shared time budget is spent (guards against catastrophic backtracking). */
    private class Bounded(
        private val inner: CharSequence,
        private val deadline: Long,
    ) : CharSequence {
        private var reads = 0

        override val length: Int get() = inner.length

        override fun get(index: Int): Char {
            if (++reads and 0x3FF == 0 && System.nanoTime() > deadline) {
                throw RegexTimeoutException()
            }
            return inner[index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
            Bounded(inner.subSequence(startIndex, endIndex), deadline)

        override fun toString(): String = inner.toString()
    }

    // Returns the info-string language of the fenced code block containing lineNumber (0-based), if any.
    private fun findCodeBlockLanguage(
        text: CharSequence,
        lineNumber: Int,
    ): String? {
        var fence: String? = null
        var language: String? = null

        for ((index, line) in text.lineSequence().withIndex()) {
            if (index > lineNumber) {
                break
            }

            val trimmed = line.trimStart()
            val marker = if (trimmed.startsWith("```")) "`" else if (trimmed.startsWith("~~~")) "~" else null

            if (marker == null) {
                continue
            }

            val run = trimmed.takeWhile { it.toString() == marker }

            if (fence == null) {
                fence = run
                language = trimmed.drop(run.length).trim().split(Regex("[\\s{]"), 2).first().ifEmpty { null }
            } else if (run.startsWith(fence) && trimmed.drop(run.length).isBlank()) {
                if (index == lineNumber) {
                    continue
                }
                fence = null
                language = null
            }
        }

        return if (fence != null) language else null
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

    private companion object {
        const val REGEX_BUDGET_NANOS = 500_000_000L
    }
}
