package com.cornmell.superjump

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import javax.swing.JComponent

class UrlSettingsConfigurable : Configurable {
    private var settingsPanel: UrlSettingsPanel? = null
    private val settings = UrlSettings.getInstance()

    override fun getDisplayName(): String = "SuperJump URL Settings"

    override fun createComponent(): JComponent {
        settingsPanel = UrlSettingsPanel(settings)
        return settingsPanel!!.getPanel()
    }

    override fun isModified(): Boolean = settingsPanel?.isModified() ?: false

    override fun apply() {
        settingsPanel?.apply(settings)
    }

    override fun reset() {
        settingsPanel?.reset(settings)
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}

class UrlSettingsConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable = UrlSettingsConfigurable()
}
