package com.cornmell.superjump.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SuperJumpSettingsTest {
    @Test
    fun `enabled flag of every jumper is carried into the runtime jumper`() {
        val defaults = SuperJumpSettings.defaultJumpers()

        for (enabled in listOf(true, false)) {
            val settings = SuperJumpSettings()
            settings.updateJumpers(defaults.map { it.copy(enabled = enabled) })

            val runtime = settings.runtimeJumpers()
            assertEquals(defaults.size, runtime.size)
            runtime.forEachIndexed { index, jumper ->
                assertEquals("${jumper.name} enabled=$enabled", enabled, jumper.enabled)
                assertEquals(defaults[index].name, jumper.name)
            }
        }
    }

    @Test
    fun `toggling a single jumper only changes that jumper`() {
        val defaults = SuperJumpSettings.defaultJumpers()

        for (target in defaults.indices) {
            val settings = SuperJumpSettings()
            settings.updateJumpers(defaults.mapIndexed { i, j -> j.copy(enabled = i != target) })

            settings.runtimeJumpers().forEachIndexed { i, jumper ->
                assertEquals("${jumper.name} with only ${defaults[target].name} disabled", i != target, jumper.enabled)
            }
        }
    }
}
