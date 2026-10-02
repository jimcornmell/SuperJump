package com.cornmell.superjump

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.cornmell.superjump.UrlSettings",
    storages = [Storage("superjump.xml")]
)
class UrlSettings : PersistentStateComponent<UrlSettings> {
    var mvnRepo: String = "https://central.sonatype.com/artifact"
    var hexLink: String = "https://www.colorhexa.com"
    var gitLink: String = "https://github.com"
    var jiraLink: String = "https://somecompanyname.atlassian.net/browse"
    var cveLink: String = "https://ossindex.sonatype.org/vulnerability"
    var chtLink: String = "https://cheat.sh"

    override fun getState(): UrlSettings = this

    override fun loadState(state: UrlSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): UrlSettings = ApplicationManager.getApplication().getService(UrlSettings::class.java)
    }
}
