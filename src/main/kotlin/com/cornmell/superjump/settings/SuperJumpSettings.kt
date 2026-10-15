package com.cornmell.superjump.settings

import com.cornmell.superjump.model.Jumper
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlinx.serialization.Serializable

@Service
@State(
    name = "com.cornmell.superjump.SuperJumpSettings",
    storages = [Storage("superJump.xml")],
)
class SuperJumpSettings : SerializablePersistentStateComponent<SuperJumpSettings.State>(State()) {
    @Serializable
    data class State(
        val jumpers: List<JumperState> = defaultJumpers(),
    )

    val jumpers: List<JumperState>
        get() = state.jumpers

    fun updateJumpers(jumpers: List<JumperState>) {
        updateState {
            it.copy(jumpers = jumpers.toList())
        }
    }

    fun runtimeJumpers(): List<Jumper> =
        state.jumpers.map { jumper ->
            Jumper(
                enabled = jumper.enabled,
                name = jumper.name,
                regex = Regex(jumper.regex),
                url = jumper.url,
                fileRegex =
                    jumper.fileRegex
                        ?.takeIf { it.isNotBlank() }
                        ?.let(::Regex),
            )
        }

    companion object {
        fun getInstance(): SuperJumpSettings =
            com.intellij.openapi.application.ApplicationManager
                .getApplication()
                .getService(SuperJumpSettings::class.java)

        fun defaultJumpers(): List<JumperState> =
            listOf(
                JumperState(
                    enabled = true,
                    name = "URL",
                    regex = """.*(https?://[^\s\"'<>]+).*""",
                    url = $$"$1",
                ),
                JumperState(
                    enabled = true,
                    name = "CVE",
                    regex = """\b(CVE-\d{4}-\d{4,7})\b""",
                    url = $$"https://ossindex.sonatype.org/vulnerability/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Gradle Coordinate",
                    regex = """([0-9A-Za-z-\.]+):([0-9A-Za-z-\.]+):([0-9A-Za-z-\.]+)""",
                    url = $$"https://central.sonatype.com/artifact/$1/$2/$3",
                ),
                JumperState(
                    enabled = true,
                    name = "Gradle Coordinate No Version",
                    regex = """([0-9A-Za-z-\.]+):([0-9A-Za-z-\.]+)""",
                    url = $$"https://central.sonatype.com/artifact/$1/$2",
                ),
                JumperState(
                    enabled = true,
                    name = "Colour",
                    regex = """#?([0-9a-fA-F]{6})""",
                    url = $$"https://www.colorhexa.com/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Repository",
                    regex = """([A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+)""",
                    url = $$"https://github.com/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Jira Key",
                    regex = """([A-Z]{2,6}-[0-9]{2,7})""",
                    url = $$"https://somecompanyname.atlassian.net/browse/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Docker Hub Image",
                    regex = """(?i)^FROM\s+([a-z0-9._-]+)""",
                    url = $$"https://hub.docker.com/_/$1",
                    fileRegex = """(^|/)Dockerfile$""",
                ),
                JumperState(
                    enabled = true,
                    name = "Linux command",
                    regex = """([a-z]+)""",
                    url = $$"https://cheat.sh/$1",
                ),
            )
    }
}
