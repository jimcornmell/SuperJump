package com.cornmell.superjump

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal object GitFileUrl {
    private val scpRemote = Regex("""(?:[^@/]+@)?([^/:]+):(.+)""")

    fun build(
        remoteUrl: String,
        revision: String,
        filePath: String,
        lineNumber: Int,
        detached: Boolean = false,
    ): String {
        val uri = if (remoteUrl.contains("://")) {
            URI(remoteUrl)
        } else {
            val match = requireNotNull(scpRemote.matchEntire(remoteUrl)) { "Unsupported Git remote format" }
            URI("ssh", null, match.groupValues[1], -1, "/${match.groupValues[2]}", null, null)
        }
        val host = requireNotNull(uri.host) { "Git remote has no host" }.lowercase()
        require(uri.scheme in listOf("http", "https", "ssh", "git")) { "Unsupported Git remote scheme" }
        val scheme = if (uri.scheme == "http") "http" else "https"
        val port = if (uri.scheme in listOf("http", "https")) uri.port else -1
        val origin = URI(scheme, null, host, port, null, null, null).toASCIIString()
        val repositoryPath = uri.path.trim('/').removeSuffix(".git")
        require(repositoryPath.isNotEmpty()) { "Git remote has no repository path" }
        val path = encodePath(filePath)
        val ref = encode(revision)
        val line = lineNumber + 1

        if (host == "ssh.dev.azure.com" || host == "dev.azure.com" || host.endsWith(".visualstudio.com")) {
            val webPath = if (host == "ssh.dev.azure.com") {
                val parts = repositoryPath.removePrefix("v3/").split('/')
                require(parts.size == 3) { "Unsupported Azure DevOps SSH remote" }
                "${encode(parts[0])}/${encode(parts[1])}/_git/${encode(parts[2])}"
            } else {
                encodePath(repositoryPath)
            }
            val webOrigin = if (host == "ssh.dev.azure.com") "https://dev.azure.com" else origin
            val version = encode((if (detached) "GC" else "GB") + revision)
            return "$webOrigin/$webPath?path=${encode("/$filePath")}&version=$version&line=$line&lineEnd=${line + 1}&lineStartColumn=1&lineEndColumn=1&lineStyle=plain&_a=contents"
        }

        val repositoryUrl = "$origin/${encodePath(repositoryPath)}"
        return when {
            host == "bitbucket.org" -> "$repositoryUrl/src/$ref/$path#lines-$line"
            host == "gitlab.com" || host.startsWith("gitlab.") ->
                "$repositoryUrl/-/blob/$ref/$path#L$line"
            else -> "$repositoryUrl/blob/$ref/$path#L$line"
        }
    }

    private fun encodePath(path: String): String = path.split('/').joinToString("/") { encode(it) }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}
