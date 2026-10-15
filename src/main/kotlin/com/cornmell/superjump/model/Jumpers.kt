package com.cornmell.superjump.model

import com.cornmell.superjump.settings.SuperJumpSettings

object Jumpers {
    val all: List<Jumper>
        get() =
            SuperJumpSettings
                .getInstance()
                .runtimeJumpers()
}
