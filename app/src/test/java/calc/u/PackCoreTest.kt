package calc.u

import calc.u.packs.CatalogEntry
import calc.u.packs.PackManifest
import calc.u.packs.PackTool
import calc.u.packs.decodeEnabled
import calc.u.packs.encodeEnabled
import calc.u.packs.encodeManifest
import calc.u.packs.jsCall
import calc.u.packs.jsQuote
import calc.u.packs.parseCatalog
import calc.u.packs.parseManifest
import calc.u.packs.sha256Hex
import calc.u.packs.validEntryName
import calc.u.packs.validPackId
import calc.u.packs.verifyEntry
import org.junit.Assert.*
import org.junit.Test

class PackCoreTest {
    private fun sample() = PackManifest(
        id = "text-extra",
        version = 1,
        name = "Text Extra",
        runtime = "duktape-js-1",
        entry = "main.js",
        sha256 = sha256Hex("x".toByteArray()),
        tools = listOf(PackTool("slugify", "Slugify", "slugify"))
    )

    @Test fun manifestRoundTrip() {
        assertEquals(sample(), parseManifest(encodeManifest(sample())))
    }

    @Test fun catalogParse() {
        val raw = """{"packs":[{"id":"a","version":2,"name":"A","url":"asset://x","sha256":"00"}]}"""
        val cat = parseCatalog(raw)
        assertEquals(1, cat.packs.size)
        assertEquals(CatalogEntry("a", 2, "A", "", "asset://x", "00"), cat.packs[0])
        assertEquals(0, parseCatalog("not json{{{").packs.size)
    }

    @Test fun hashVerify() {
        val bytes = "hello pack".toByteArray()
        val hex = sha256Hex(bytes)
        assertTrue(verifyEntry(bytes, hex))
        assertTrue(verifyEntry(bytes, hex.uppercase()))
        assertFalse(verifyEntry(bytes, "00"))
        assertFalse(verifyEntry(bytes, ""))
    }

    @Test fun jsCallEscapes() {
        assertEquals("slugify(\"Hi\")", jsCall("slugify", "Hi"))
        assertEquals("f(\"a\\\"b\\\\c\\nd\")", jsCall("f", "a\"b\\c\nd"))
        try {
            jsCall("a-b", "x")
            fail("expected failure for bad function name")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test fun idValidation() {
        assertTrue(validPackId("text-extra"))
        assertTrue(validPackId("a1"))
        assertFalse(validPackId(""))
        assertFalse(validPackId("UPPER"))
        assertFalse(validPackId("../escape"))
        assertTrue(validEntryName("main.js"))
        assertFalse(validEntryName("../main.js"))
        assertFalse(validEntryName("a/b.js"))
    }

    @Test fun enabledCodec() {
        assertEquals(setOf("a", "b"), decodeEnabled(encodeEnabled(setOf("a", "b"))))
        assertEquals(emptySet<String>(), decodeEnabled("junk"))
    }
}
