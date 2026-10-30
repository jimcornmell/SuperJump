package com.cornmell.superjump.settings

import kotlinx.serialization.Serializable

@Serializable
data class JumperState(
    val enabled: Boolean = true,
    val name: String = "",
    val regex: String = "",
    val url: String = "",
    val fileRegex: String? = null,
)
