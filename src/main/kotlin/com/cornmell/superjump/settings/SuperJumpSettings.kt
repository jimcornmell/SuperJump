package com.cornmell.superjump.settings

import com.cornmell.superjump.model.Jumper
import com.intellij.openapi.application.ApplicationManager
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
class SuperJumpSettings : SerializablePersistentStateComponent<SuperJumpSettings.State>(State(version = CURRENT_VERSION)) {
    @Serializable
    data class State(
        val jumpers: List<JumperState> = defaultJumpers(),
        val version: Int = 0, // 0 = state saved before versioning existed
        val fallbackToGit: Boolean = true,
    )

    @Volatile
    private var compiled: Pair<List<JumperState>, List<Jumper>>? = null

    val jumpers: List<JumperState>
        get() = state.jumpers

    val fallbackToGit: Boolean
        get() = state.fallbackToGit

    fun updateFallbackToGit(value: Boolean) {
        updateState { it.copy(fallbackToGit = value) }
    }

    fun updateJumpers(jumpers: List<JumperState>) {
        updateState {
            it.copy(jumpers = jumpers.toList(), version = CURRENT_VERSION)
        }
    }

    override fun loadState(state: State) {
        super.loadState(migrate(state))
    }

    /** Compiled jumpers, rebuilt only when the persisted list instance changes. */
    fun runtimeJumpers(): List<Jumper> {
        val source = state.jumpers
        compiled?.takeIf { it.first === source }?.let { return it.second }

        return toRuntimeJumpers(source).also { compiled = source to it }
    }

    companion object {
        private const val CURRENT_VERSION = 1

        private val defaultRuntime: List<Jumper> by lazy { toRuntimeJumpers(defaultJumpers()) }

        /** Jumpers for the running IDE, or the defaults when no IDE application exists (e.g. plain unit tests). */
        fun currentRuntimeJumpers(): List<Jumper> =
            if (ApplicationManager.getApplication() == null) {
                defaultRuntime
            } else {
                getInstance().runtimeJumpers()
            }

        /** Jumpers whose regexes do not compile are skipped rather than breaking every jump. */
        private fun toRuntimeJumpers(jumpers: List<JumperState>): List<Jumper> =
            jumpers.mapNotNull { jumper ->
                runCatching {
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
                }.getOrNull()
            }

        /** Add one step per version here when defaults change (e.g. append newly added default jumpers by name). */
        private fun migrate(state: State): State =
            if (state.version >= CURRENT_VERSION) state else state.copy(version = CURRENT_VERSION)

        fun getInstance(): SuperJumpSettings =
            ApplicationManager
                .getApplication()
                .getService(SuperJumpSettings::class.java)

        fun defaultJumpers(): List<JumperState> =
            listOf(
                JumperState(
                    enabled = true,
                    name = "URL",
                    regex = """(?i).*?(https?://[^\s<>"']+).*""",
                    url = $$"$1",
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
                    name = "CVE",
                    regex = """(?i)\b(CVE-\d{4}-\d{4,})\b""",
                    url = $$"https://ossindex.sonatype.org/vulnerability/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Gradle Coordinate",
                    regex = """([0-9A-Za-z-_\.]+):([0-9A-Za-z-_\.]+):([0-9A-Za-z-_\.]+)""",
                    url = $$"https://central.sonatype.com/artifact/$1/$2/$3",
                    fileRegex = """(^|/)(build.gradle|build.gradle.groovy|build.gradle.kts|settings.gradle|settings.gradle.groovy|settings.gradle.kts|groovy)$""",
                ),
                JumperState(
                    enabled = true,
                    name = "Gradle Coordinate No Version",
                    regex = """([0-9A-Za-z-_\.]+):([0-9A-Za-z-_\.]+)""",
                    url = $$"https://central.sonatype.com/artifact/$1/$2",
                    fileRegex = """(^|/)(build.gradle|build.gradle.groovy|build.gradle.kts|settings.gradle|settings.gradle.groovy|settings.gradle.kts|groovy)$""",
                ),
                JumperState(
                    enabled = true,
                    name = "Colour",
                    regex = """(?<![0-9A-Za-z])#?([0-9a-fA-F]{6})(?![0-9A-Za-z])""",
                    url = $$"https://www.colorhexa.com/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Jira Key",
                    regex = """([A-Z]{2,6}-[0-9]{2,7})""",
                    url = $$"https://somecompanyname.atlassian.net/browse/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Repository",
                    regex = """(?<![A-Za-z0-9_.-])([A-Za-z0-9][A-Za-z0-9_.-]*/[A-Za-z0-9_.-]+)""",
                    url = $$"https://github.com/$1",
                ),
                JumperState(
                    enabled = true,
                    name = "Linux command",
                    regex = """\b([a-z]+)\b""",
                    // url = $$"https://cheat.sh/$1",
                    url = $$"https://tldr.inbrowser.app/pages/common/$1",
                    fileRegex = """(?i)(.*\.)?(console|shell|sh|bash|zsh|ksh|fish)$""",
                ),
            )
    }
}
