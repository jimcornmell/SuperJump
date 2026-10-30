package com.cornmell.superjump.model

data class Jumper(
    val enabled: Boolean,
    val name: String,
    val regex: Regex,
    val url: String,
    val fileRegex: Regex? = null,
) {
    /** Case-insensitive variant used to match markdown code block languages. */
    val fileRegexIgnoreCase: Regex? = fileRegex?.let { Regex(it.pattern, RegexOption.IGNORE_CASE) }
}
