package com.cornmell.superjump

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSupportTest {
    private fun match(regex: String, text: String) = Regex(regex).find(text)!!

    @Test
    fun `placeholders above nine are not confused with lower ones`() {
        val m = match("(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)", "abcdefghij")
        assertEquals("a-j", UrlSupport.expand("\$1-\$10", m))
    }

    @Test
    fun `captured text is not re-expanded and unsafe characters are encoded`() {
        val m = match("(.+)", "x y\$2")
        assertEquals("https://e.com/x%20y\$2", UrlSupport.expand("https://e.com/\$1", m))
    }

    @Test
    fun `unknown groups are left untouched`() {
        assertEquals("a/\$5", UrlSupport.expand("\$1/\$5", match("(a)", "a")))
    }

    @Test
    fun `only http and https urls are browsable`() {
        assertTrue(UrlSupport.isBrowsable("https://example.com/a"))
        assertFalse(UrlSupport.isBrowsable("file:///etc/passwd"))
        assertFalse(UrlSupport.isBrowsable("javascript:alert(1)"))
        assertFalse(UrlSupport.isBrowsable(""))
        assertFalse(UrlSupport.isBrowsable(null))
    }
}
