package com.cornmell.superjump

import java.io.File

class TextToUrl {
    private fun obtainSettings(): UrlSettings {
        return try {
            UrlSettings.getInstance()
        } catch (e: Exception) {
            // Fallback for tests or when application context is not available
            UrlSettings()
        }
    }

    private val settings by lazy { obtainSettings() }

    fun toUrl(lineNumber: Int, columnNumber: Int, text: String): String? {
        val value = text.trim().removeSurrounding("\"").removeSurrounding("'")

        if (value.isEmpty()) {
            return ""
        }

        // Check for direct URLs
        val directUrl = Regex("https?://[^\\s\"'<>]+", RegexOption.IGNORE_CASE).find(value)?.value

        if (directUrl != null && (directUrl.startsWith("http://") || directUrl.startsWith("https://"))) {
            return directUrl
        }

        // Check for Maven XML markers
        if (value.lowercase().contains("<groupid>") || value.lowercase().contains("<artifactid>") || value.lowercase()
                .contains("<version>")
        ) {
            val lower = value.lowercase()
            return when {
                lower.contains("<groupid>") -> {
                    val groupId = Regex("(?i)<groupid>(.*?)</groupid>").find(value)?.groupValues?.get(1)
                    if (groupId.isNullOrBlank()) "" else "${settings.mvnRepo}/$groupId"
                }

                lower.contains("<artifactid>") -> {
                    val artifactId = Regex("(?i)<artifactid>(.*?)</artifactid>").find(value)?.groupValues?.get(1)
                    val groupId = Regex("(?i)<groupid>(.*?)</groupid>").find(value)?.groupValues?.get(1)
                    if (artifactId.isNullOrBlank() || groupId.isNullOrBlank()) "" else "${settings.mvnRepo}/$groupId/$artifactId"
                }

                lower.contains("<version>") -> {
                    val groupId = Regex("(?i)<groupid>(.*?)</groupid>").find(value)?.groupValues?.get(1)
                    val artifactId = Regex("(?i)<artifactid>(.*?)</artifactid>").find(value)?.groupValues?.get(1)
                    val version = Regex("(?i)<version>(.*?)</version>").find(value)?.groupValues?.get(1)
                    if (groupId.isNullOrBlank() || artifactId.isNullOrBlank() || version.isNullOrBlank()) "" else "${settings.mvnRepo}/$groupId/$artifactId/$version"
                }

                else -> ""
            }
        }

        // Check for CVE identifiers (case-insensitive)
        val cve = Regex("(?i)cve[-_ ]?[0-9]{4}[-_ ]?[0-9]{4,}").find(value)?.value

        if (cve != null) {
            return "${settings.cveLink}/${cve.uppercase()}"
        }

        // Check for Jira keys (e.g., ABC-123)
        val jiraKey = Regex("^[A-Za-z]{2,6}-[0-9]{2,7}$").matchEntire(value)

        if (jiraKey != null) {
            return "${settings.jiraLink}/${value.uppercase()}"
        }

        // Check for simple commands
        val command = Regex("^[A-Za-z0-9_.-]+$").matchEntire(value)

        if (command != null && isACommand(command)) {
            return "${settings.chtLink}/$value"
        }

        // Check for Gradle coordinates (e.g., group:artifact:version)
        val gradleCoordinate = Regex("[0-9A-Za-z.-]+:[0-9A-Za-z.-]+(?::[0-9A-Za-z.-]*)?").find(value)?.value

        if (gradleCoordinate != null) {
            val path = gradleCoordinate.replace(":", "/")
            return "${settings.mvnRepo}/$path"
        }

        // Check for repository slugs (e.g., owner/repository)
        val repoSlug = Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$").matchEntire(value)

        if (repoSlug != null) {
            return "${settings.gitLink}/$value"
        }

        // Check for hex colors
        val hex = Regex("#?([0-9a-fA-F]{6})").find(value)?.value

        if (hex != null) {
            val normalized = hex.trim().removePrefix("#")
            return "${settings.hexLink}/${normalized.lowercase()}"
        }

        return ""
    }

    private fun isACommand(command: MatchResult): Boolean {
        val path = System.getenv("PATH") ?: return false
        val commandName = command.value
        val isWindows = System.getProperty("os.name").contains("win", ignoreCase = true)
        val extensions = if (isWindows) {
            (System.getenv("PATHEXT") ?: ".EXE;.CMD;.BAT;.COM")
                .split(";")
                .filter { it.isNotEmpty() }
        } else {
            listOf("")
        }

        return path.split(File.pathSeparator).any { directory ->
            extensions.any { extension ->
                File(directory, commandName + extension).let { it.isFile && it.canExecute() }
            }
        }
    }
}
