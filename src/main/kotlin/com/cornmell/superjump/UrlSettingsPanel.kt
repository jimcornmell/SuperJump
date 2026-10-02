package com.cornmell.superjump

import com.intellij.openapi.util.IconLoader
import javax.swing.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets

class UrlSettingsPanel(private val settings: UrlSettings) {
    private val mvnRepoField = JTextField(50)
    private val hexLinkField = JTextField(50)
    private val gitLinkField = JTextField(50)
    private val jiraLinkField = JTextField(50)
    private val cveLinkField = JTextField(50)
    private val chtLinkField = JTextField(50)

    private val originalSettings = UrlSettings()

    init {
        reset(settings)
    }

    fun getPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }

        var row = 0

        // Title
        val titleLabel = JLabel(
            "SuperJump URL Configuration",
            IconLoader.getIcon("/icons/panelIcon.svg", javaClass),
            SwingConstants.LEFT
        )
        titleLabel.font = titleLabel.font.deriveFont(14f)
        gbc.gridx = 0
        gbc.gridy = row
        gbc.gridwidth = 2
        panel.add(titleLabel, gbc)
        row++

        gbc.gridwidth = 1

        // Maven Repository URL
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        panel.add(JLabel("Maven Repository URL:"), gbc)
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(mvnRepoField, gbc)
        row++

        // Hex Color URL
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        panel.add(JLabel("Color URL:"), gbc)
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(hexLinkField, gbc)
        row++

        // Git URL
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        panel.add(JLabel("Git URL:"), gbc)
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(gitLinkField, gbc)
        row++

        // Jira URL
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        panel.add(JLabel("Jira URL:"), gbc)
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(jiraLinkField, gbc)
        row++

        // CVE URL
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        panel.add(JLabel("CVE Link URL:"), gbc)
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(cveLinkField, gbc)
        row++

        // Cheat Sheet URL
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        panel.add(JLabel("Cheat Sheet URL:"), gbc)
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(chtLinkField, gbc)
        row++

        // Reset Button
        val resetButton = JButton("Reset to Defaults")
        resetButton.addActionListener {
            resetToDefaults()
        }
        gbc.gridx = 1
        gbc.gridy = row
        gbc.weightx = 0.0
        gbc.anchor = GridBagConstraints.EAST
        panel.add(resetButton, gbc)

        // Add a filler to push content to the top
        gbc.gridx = 0
        gbc.gridy = row + 1
        gbc.gridwidth = 2
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(JPanel(), gbc)

        return panel
    }

    fun isModified(): Boolean {
        return mvnRepoField.text != originalSettings.mvnRepo ||
                hexLinkField.text != originalSettings.hexLink ||
                gitLinkField.text != originalSettings.gitLink ||
                jiraLinkField.text != originalSettings.jiraLink ||
                cveLinkField.text != originalSettings.cveLink ||
                chtLinkField.text != originalSettings.chtLink
    }

    fun apply(settings: UrlSettings) {
        settings.mvnRepo = mvnRepoField.text
        settings.hexLink = hexLinkField.text
        settings.gitLink = gitLinkField.text
        settings.jiraLink = jiraLinkField.text
        settings.cveLink = cveLinkField.text
        settings.chtLink = chtLinkField.text
        updateOriginalSettings()
    }

    fun reset(settings: UrlSettings) {
        mvnRepoField.text = settings.mvnRepo
        hexLinkField.text = settings.hexLink
        gitLinkField.text = settings.gitLink
        jiraLinkField.text = settings.jiraLink
        cveLinkField.text = settings.cveLink
        chtLinkField.text = settings.chtLink
        updateOriginalSettings()
    }

    private fun updateOriginalSettings() {
        originalSettings.mvnRepo = mvnRepoField.text
        originalSettings.hexLink = hexLinkField.text
        originalSettings.gitLink = gitLinkField.text
        originalSettings.jiraLink = jiraLinkField.text
        originalSettings.cveLink = cveLinkField.text
        originalSettings.chtLink = chtLinkField.text
    }

    private fun resetToDefaults() {
        mvnRepoField.text = "https://central.sonatype.com/artifact"
        hexLinkField.text = "https://www.colorhexa.com"
        gitLinkField.text = "https://github.com"
        jiraLinkField.text = "https://somecompanyname.atlassian.net/browse"
        cveLinkField.text = "https://ossindex.sonatype.org/vulnerability"
        chtLinkField.text = "https://cheat.sh"
    }
}
