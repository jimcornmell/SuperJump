package com.cornmell.superjump

import org.junit.Assert.assertEquals
import org.junit.Test

class TextToUrlTest {
    private val textToUrl = TextToUrl()

    @Test
    fun `returns the GitHub root for blank text`() {
        assertEquals("", textToUrl.toUrl(1, 1, "  "))
    }

    @Test
    fun `returns the GitHub root for double-quoted empty text`() {
        assertEquals("", textToUrl.toUrl(1, 1, "\"\""))
    }

    @Test
    fun `returns the GitHub root for single-quoted empty text`() {
        assertEquals("", textToUrl.toUrl(1, 1, "''"))
    }

    @Test
    fun `returns a quoted https url`() {
        assertEquals("https://example.com/path", textToUrl.toUrl(1, 1, "\"https://example.com/path\""))
    }

    @Test
    fun `returns an http url at the start of text`() {
        assertEquals("http://example.com", textToUrl.toUrl(1, 1, "http://example.com more text"))
    }

    @Test
    fun `returns an http url in the middle of text`() {
        assertEquals("http://example.com", textToUrl.toUrl(1, 1, "some text at the start http://example.com more text"))
    }

    @Test
    fun `returns an https url in the middle of text`() {
        assertEquals("https://example.com", textToUrl.toUrl(1, 1, "some text at the start https://example.com more text"))
    }

    @Test
    fun `returns a calendar https url in the middle of text`() {
        assertEquals(
            "https://calendar.google.com/calendar/u/0/r",
            textToUrl.toUrl(1, 1, "some text at the start https://calendar.google.com/calendar/u/0/r more text")
        )
    }

    @Test
    fun `converts group id xml to a Maven Central URL`() {
        assertEquals(
            "https://central.sonatype.com/artifact/com.example",
            textToUrl.toUrl(1, 1, "<groupId>com.example</groupId>")
        )
    }

    @Test
    fun `returns empty string for incomplete Maven xml`() {
        assertEquals("", textToUrl.toUrl(1, 1, "<groupId></groupId>"))
    }

    @Test
    fun `converts CVE identifiers to the vulnerability index`() {
        assertEquals(
            "https://ossindex.sonatype.org/vulnerability/CVE-2024-12345",
            textToUrl.toUrl(1, 1, "some text cve-2024-12345 some text")
        )
    }

    @Test
    fun `converts long hex colors`() {
        assertEquals("https://www.colorhexa.com/a1b2c3", textToUrl.toUrl(1, 1, "A1B2C3"))
    }

    @Test
    fun `searches for text containing a hex color with hash`() {
        assertEquals("https://www.colorhexa.com/a1b2c3", textToUrl.toUrl(1, 1, "some #A1B2C3 text"))
    }

    @Test
    fun `searches for text containing a hex color without hash`() {
        assertEquals("https://www.colorhexa.com/a1b2c3", textToUrl.toUrl(1, 1, "some A1B2C3 text"))
    }

    @Test
    fun `converts Jira keys to the issue browser`() {
        assertEquals(
            "https://somecompanyname.atlassian.net/browse/ABC-123",
            textToUrl.toUrl(1, 1, "abc-123")
        )
    }

    @Test
    fun `converts Gradle coordinates to Maven Central paths`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.example/library/1.2.3",
            textToUrl.toUrl(1, 1, "org.example:library:1.2.3")
        )
    }

    @Test
    fun `converts Gradle coordinates from within line to Maven Central paths`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.example/library/1.2.3",
            textToUrl.toUrl(1, 1, "testImplementation(\"org.example:library:1.2.3\")")
        )
    }

    @Test
    fun `routes repository slugs to GitHub`() {
        assertEquals("https://github.com/owner/repository", textToUrl.toUrl(1, 1, "owner/repository"))
    }

    @Test
    fun `routes repository slugs to GitLab`() {
        assertEquals(
            "https://github.com/some/repository",
            textToUrl.toUrl(1, 1, "some/repository")
        )
    }

    @Test
    fun `routes simple commands to cheat sh`() {
        assertEquals("https://cheat.sh/kubectl", textToUrl.toUrl(1, 1, "kubectl"))
    }

    @Test
    fun `URL encodes unrecognized text as a GitHub search`() {
        assertEquals(
            "",
            textToUrl.toUrl(1, 1, "two words/here")
        )
    }

    @Test
    fun `super jump searches for direct URLs instead of opening them`() {
        assertEquals(
            "https://example.com/path",
            textToUrl.toUrl(1, 1, "https://example.com/path")
        )
    }

    @Test
    fun `returns empty string for artifactId XML without groupId`() {
        assertEquals("", textToUrl.toUrl(1, 1, "<artifactId>library</artifactId>"))
    }

    @Test
    fun `returns empty string for version XML without required Maven values`() {
        assertEquals("", textToUrl.toUrl(1, 1, "<version>1.2.3</version>"))
    }

    @Test
    fun `recognizes case-insensitive CVE identifiers with underscores`() {
        assertEquals(
            "https://ossindex.sonatype.org/vulnerability/CVE_2024_12345",
            textToUrl.toUrl(1, 1, "cve_2024_12345")
        )
    }

    @Test
    fun `recognizes CVE identifiers with spaces as separators`() {
        assertEquals(
            "https://ossindex.sonatype.org/vulnerability/CVE 2024 12345",
            textToUrl.toUrl(1, 1, "CVE 2024 12345")
        )
    }

    @Test
    fun `recognizes command`() {
        assertEquals("https://cheat.sh/kubectl", textToUrl.toUrl(1, 1, "  'kubectl'  "))
    }

    @Test
    fun `recognizes gradle repo`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.example/library/1.2.3",
            textToUrl.toUrl(1, 1, "\"org.example:library:1.2.3\"")
        )
    }

    @Test
    fun `recognizes gradle repo with number`() {
        assertEquals(
            "https://central.sonatype.com/artifact/org.eclipse.jdt/junit/3.3.0-v20070606-0010",
            textToUrl.toUrl(1, 1, "\"org.eclipse.jdt:junit:3.3.0-v20070606-0010\"")
        )
    }
}
