package com.cornmell.superjump.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SuperJumpConfigurableTest {
    @Test
    fun `enabled column uses the boxed Boolean class so a checkbox is rendered`() {
        val model = SuperJumpConfigurable.JumperTableModel(SuperJumpSettings.defaultJumpers())

        assertEquals(Boolean::class.javaObjectType, model.getColumnClass(0))
    }
}
