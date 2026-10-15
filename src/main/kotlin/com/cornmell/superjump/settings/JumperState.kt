package com.cornmell.superjump.settings

import kotlinx.serialization.Serializable

@Serializable
data class JumperState(
    var enabled: Boolean = true,
    var name: String = "",
    var regex: String = "",
    var url: String = "",
    var fileRegex: String? = null,
)
