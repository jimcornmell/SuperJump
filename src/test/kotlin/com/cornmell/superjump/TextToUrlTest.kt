package com.cornmell.superjump

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class TextToUrlTest {
    private val textToUrl = TextToUrl()

    @Test
    fun `selection uses the whole selected text instead of the document column`() {
        assertEquals(
            "https://www.colorhexa.com/112233",
            textToUrl.toUrl("notes.txt", 0, -1, "#112233 #445566")
        )
    }

    @Test
    fun `character offset after a tab identifies the current word`() {
        val line = "\t#112233 #445566"
        assertEquals(
            "https://www.colorhexa.com/445566",
            textToUrl.toUrl("notes.txt", 0, line.indexOf("445566")+2, line)
        )
    }

    @Test
    fun `returns the GitHub root for blank text`() {
        assertNull(textToUrl.toUrl("", 1, 1, "  "))
    }

    @Test
    fun `returns the GitHub root for double-quoted empty text`() {
        assertNull(textToUrl.toUrl("", 1, 1, "\"\""))
    }

    @Test
    fun `returns the GitHub root for single-quoted empty text`() {
        assertNull(textToUrl.toUrl("", 1, 1, "''"))
    }

    @Test
    fun `returns a quoted https url`() {
        assertEquals("https://example.com/path", textToUrl.toUrl("", 1, 1, "\"https://example.com/path\""))
    }

    @Test
    fun `returns an http url at the start of text`() {
        assertEquals("http://example.com", textToUrl.toUrl("", 1, 1, "http://example.com more text"))
    }

    @Test
    fun `returns an http url in the middle of text`() {
        assertEquals("http://example.com", textToUrl.toUrl("", 1, 1, "some text at the start http://example.com more text"))
    }

    @Test
    fun `returns an https url in the middle of text`() {
        assertEquals("https://example.com", textToUrl.toUrl("", 1, 1, "some text at the start https://example.com more text"))
    }

    @Test
    fun `returns a calendar https url in the middle of text`() {
        assertEquals(
            "https://calendar.google.com/calendar/u/0/r",
            textToUrl.toUrl("", 1, 1, "some text at the start https://calendar.google.com/calendar/u/0/r more text")
        )
    }

    @Test
    fun `converts CVE identifiers to the vulnerability index`() {
        assertTrue(
            "https://ossindex.sonatype.org/vulnerability/CVE-2024-12345".equals(
            textToUrl.toUrl("", 1, 15, "some text cve-2024-12345 some text"), ignoreCase = true)
        )
    }

    @Test
    fun `converts long hex colors`() {
        assertEquals("https://www.colorhexa.com/A1B2C3", textToUrl.toUrl("", 1, 1, "#A1B2C3"))
    }

    @Test
    fun `searches for text containing a hex color with hash`() {
        assertEquals("https://www.colorhexa.com/A1B2C3", textToUrl.toUrl("", 1, 1, "some #A1B2C3 text"))
    }

    @Test
    fun `searches for text containing a hex color without hash`() {
        assertTrue("https://www.colorhexa.com/A1B2C3".equals(textToUrl.toUrl("", 1, 1, "some A1B2C3 text"), ignoreCase = true))
    }

    @Test
    fun `converts Jira keys to the issue browser`() {
        assertEquals(
            "https://somecompanyname.atlassian.net/browse/ABC-123",
            textToUrl.toUrl("", 1, 1, "ABC-123")
        )
    }

    @Test
    fun `converts Gradle coordinates to Maven Central paths`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.example/library/1.2.3",
            textToUrl.toUrl("build.gradle", 1, 1, "org.example:library:1.2.3")
        )
    }

    @Test
    fun `converts Gradle coordinates from within line to Maven Central paths`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.example/library/1.2.3",
            textToUrl.toUrl("build.gradle", 1, 1, "testImplementation(\"org.example:library:1.2.3\")")
        )
    }

    @Test
    fun `routes repository slugs to GitHub`() {
        assertEquals("https://github.com/owner/repository", textToUrl.toUrl("", 1, 1, "owner/repository"))
    }

    @Test
    fun `routes repository slugs to GitLab`() {
        assertEquals(
            "https://github.com/some/repository",
            textToUrl.toUrl("", 1, 1, "some/repository")
        )
    }

    @Test
    fun `routes simple commands to cheat sh`() {
        assertEquals("https://tldr.inbrowser.app/pages/common/kubectl", textToUrl.toUrl("run.sh", 1, 1, "kubectl"))
    }

    // @Test
    // fun `URL encodes unrecognized text as a GitHub search`() {
        // assertEquals(
            // "",
            // textToUrl.toUrl("", 1, 1, "two words/here")
        // )
    // }

    @Test
    fun `super jump searches for direct URLs instead of opening them`() {
        assertEquals(
            "https://example.com/path",
            textToUrl.toUrl("", 1, 1, "https://example.com/path")
        )
    }

    fun `recognizes CVE identifiers with dashes as separators`() {
        assertEquals(
            "https://ossindex.sonatype.org/vulnerability/CVE-2024-12345",
            textToUrl.toUrl("", 1, 1, "CVE-2024-12345")
        )
    }

    @Test
    fun `recognizes command`() {
        assertEquals("https://tldr.inbrowser.app/pages/common/kubectl", textToUrl.toUrl("run.sh", 1, 1, "kubectl"))
    }

    @Test
    fun `recognizes gradle repo`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.example/library/1.2.3",
            textToUrl.toUrl("build.gradle", 1, 1, "\"org.example:library:1.2.3\"")
        )
    }

    @Test
    fun `recognizes gradle repo with number`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.eclipse.jdt/junit/3.3.0-v20070606-0010",
            textToUrl.toUrl("build.gradle", 1, 1, "\"org.eclipse.jdt:junit:3.3.0-v20070606-0010\"")
        )
    }

    @Test
    fun `recognizes Dockerfile from`() {
        assertEquals(
            "https://hub.docker.com/_/ubuntu",
            textToUrl.toUrl("Dockerfile", 1, 1, "from ubuntu:24.04")
        )
    }

    @Test
    fun `ignores from clause in a file that is not a Dockerfile`() {
        assertEquals(
            "https://central.sonatype.com/artifact/ubuntu/24.04",
            textToUrl.toUrl("build.gradle", 1, 1, "from ubuntu:24.04")
        )
    }

    @Test
    fun markdownCodeBlockLanguageMatchesFileRegex() {
        val doc = "# Title\n```dockerfile\nfrom ubuntu:24.04\n```\nfrom ubuntu:24.04\n"
        val expected = textToUrl.toUrl("Dockerfile", 1, 1, "from ubuntu:24.04")
        assertEquals(expected, textToUrl.toUrl("README.md", 2, 1, "from ubuntu:24.04", doc))
        assertEquals(textToUrl.toUrl("notes.txt", 1, 1, "from ubuntu:24.04"), textToUrl.toUrl("README.md", 4, 1, "from ubuntu:24.04", doc))
    }

    private val dockerUrl get() = textToUrl.toUrl("Dockerfile", 1, 1, "from ubuntu:24.04")
    private val plainUrl get() = textToUrl.toUrl("notes.txt", 1, 1, "from ubuntu:24.04")

    @Test
    fun markdownLanguageMatchIsCaseInsensitive() {
        val doc = "```Dockerfile\nfrom ubuntu:24.04\n```"
        assertEquals(dockerUrl, textToUrl.toUrl("a.md", 1, 1, "from ubuntu:24.04", doc))
    }

    @Test
    fun markdownLanguageWithExtraInfoStringStillMatches() {
        val doc = "```dockerfile title=\"x\"\nfrom ubuntu:24.04\n```"
        assertEquals(dockerUrl, textToUrl.toUrl("a.md", 1, 1, "from ubuntu:24.04", doc))
    }

    @Test
    fun markdownTildeFenceMatches() {
        val doc = "~~~dockerfile\nfrom ubuntu:24.04\n~~~"
        assertEquals(dockerUrl, textToUrl.toUrl("a.md", 1, 1, "from ubuntu:24.04", doc))
    }

    @Test
    fun markdownBlockWithOtherLanguageDoesNotMatch() {
        val doc = "```kotlin\nfrom ubuntu:24.04\n```"
        assertEquals(plainUrl, textToUrl.toUrl("a.md", 1, 1, "from ubuntu:24.04", doc))
    }

    @Test
    fun markdownBlockWithoutLanguageDoesNotMatch() {
        val doc = "```\nfrom ubuntu:24.04\n```"
        assertEquals(plainUrl, textToUrl.toUrl("a.md", 1, 1, "from ubuntu:24.04", doc))
    }

    @Test
    fun markdownSecondBlockUsesItsOwnLanguage() {
        val doc = "```kotlin\nval x = 1\n```\n\n```dockerfile\nfrom ubuntu:24.04\n```"
        assertEquals(dockerUrl, textToUrl.toUrl("a.md", 5, 1, "from ubuntu:24.04", doc))
        assertEquals(plainUrl, textToUrl.toUrl("a.md", 1, 1, "from ubuntu:24.04", doc))
    }

    @Test
    fun nonMarkdownFileIgnoresFencesInText() {
        val doc = "```dockerfile\nfrom ubuntu:24.04\n```"
        assertEquals(plainUrl, textToUrl.toUrl("notes.txt", 1, 1, "from ubuntu:24.04", doc))
    }
}
