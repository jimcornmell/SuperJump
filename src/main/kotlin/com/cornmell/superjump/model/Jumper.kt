package com.cornmell.superjump.model

data class Jumper(
    val enabled: Boolean,
    val name: String,
    val regex: Regex,
    val url: String,
    val fileRegex: Regex? = null,
)
