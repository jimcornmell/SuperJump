package com.cornmell.superjump

import org.junit.Assert.assertEquals
import org.junit.Test

class GitFileUrlTest {
    @Test
    fun `encodes file names and branch names without changing path separators`() {
        assertEquals(
            "https://github.com/owner/repo/blob/feature%2Flinks/src/a%20%23%3F%25.txt#L42",
            GitFileUrl.build("git@github.com:owner/repo.git", "feature/links", "src/a #?%.txt", 41)
        )
    }

    @Test
    fun `drops SSH user and port`() {
        assertEquals(
            "https://github.com/owner/repo/blob/main/file.txt#L1",
            GitFileUrl.build("ssh://alice@github.com:2222/owner/repo.git", "main", "file.txt", 0)
        )
    }

    @Test
    fun `preserves HTTP scheme and web port`() {
        assertEquals(
            "http://gitlab.example.com:8080/team/repo/-/blob/main/file.txt#L1",
            GitFileUrl.build("http://gitlab.example.com:8080/team/repo.git", "main", "file.txt", 0)
        )
    }

    @Test
    fun `uses GitLab routes for GitLab hosts`() {
        assertEquals(
            "https://gitlab.com/group/subgroup/repo/-/blob/main/file.txt#L2",
            GitFileUrl.build("git@gitlab.com:group/subgroup/repo.git", "main", "file.txt", 1)
        )
    }

    @Test
    fun `does not classify repositories named gitlab as GitLab hosts`() {
        assertEquals(
            "https://github.com/owner/gitlab-tools/blob/main/file.txt#L1",
            GitFileUrl.build("https://github.com/owner/gitlab-tools.git", "main", "file.txt", 0)
        )
    }

    @Test
    fun `uses Bitbucket Cloud routes and line anchors`() {
        assertEquals(
            "https://bitbucket.org/team/repo/src/main/file.txt#lines-3",
            GitFileUrl.build("git@bitbucket.org:team/repo.git", "main", "file.txt", 2)
        )
    }

    @Test
    fun `converts Azure SSH remotes to web routes`() {
        assertEquals(
            "https://dev.azure.com/org/project/_git/repo?path=%2Fsrc%2Fa%20%23.txt&version=GBfeature%2Flinks&line=4&lineEnd=5&lineStartColumn=1&lineEndColumn=1&lineStyle=plain&_a=contents",
            GitFileUrl.build("git@ssh.dev.azure.com:v3/org/project/repo", "feature/links", "src/a #.txt", 3)
        )
    }

    @Test
    fun `uses commit versions for detached Azure checkouts`() {
        assertEquals(
            "https://dev.azure.com/org/project/_git/repo?path=%2Ffile.txt&version=GCabc123&line=1&lineEnd=2&lineStartColumn=1&lineEndColumn=1&lineStyle=plain&_a=contents",
            GitFileUrl.build("https://org@dev.azure.com/org/project/_git/repo", "abc123", "file.txt", 0, true)
        )
    }

    @Test
    fun `uses current commit for detached GitHub checkouts`() {
        assertEquals(
            "https://github.com/owner/repo/blob/abc123/file.txt#L1",
            GitFileUrl.build("https://github.com/owner/repo.git", "abc123", "file.txt", 0, true)
        )
    }

    @Test
    fun `does not double encode escaped remote paths`() {
        assertEquals(
            "https://gitlab.com/team/my%20repo/-/blob/main/file.txt#L1",
            GitFileUrl.build("https://gitlab.com/team/my%20repo.git", "main", "file.txt", 0)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects local remotes rather than producing a misleading web URL`() {
        GitFileUrl.build("/home/user/repo.git", "main", "file.txt", 0)
    }
}
