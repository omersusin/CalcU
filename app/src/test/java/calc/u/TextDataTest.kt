package calc.u

import calc.u.core.TextData
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class TextDataTest {
    @Test fun sha256Known() {
        assertTrue(TextData.sha256("abc").startsWith("ba7816bf"))
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            TextData.sha256("abc")
        )
    }

    @Test fun md5Known() {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", TextData.md5("abc"))
    }

    @Test fun base64RoundTrip() {
        val original = "hello, CalcU!"
        assertEquals(original, TextData.base64Decode(TextData.base64Encode(original)))
    }

    @Test fun counts() {
        val text = "hi\nbye bye"
        assertEquals(3, TextData.wordCount(text))
        assertEquals(2, TextData.lineCount(text))
        assertEquals(10, TextData.charCount(text))
        assertEquals(Triple(3, 10, 2), TextData.counts(text))
    }

    @Test fun uuidParses() {
        UUID.fromString(TextData.uuid())
    }

    @Test fun qrMatrixSize() {
        val matrix = TextData.qrMatrix("abc", 200)
        assertEquals(200, matrix.width)
        assertEquals(200, matrix.height)
    }

    @Test fun morseSos() {
        assertEquals("... --- ...", TextData.morseEncode("SOS"))
        assertEquals("SOS", TextData.morseDecode("... --- ..."))
    }

    @Test fun binaryA() {
        assertEquals("01000001", TextData.textToBinary("A"))
        assertEquals("A", TextData.binaryToText("01000001"))
    }

    @Test fun jsonPrettyIndent() {
        val pretty = TextData.jsonPretty("{\"a\":[1,2]}")
        assertTrue(pretty.contains("\n"))
        assertTrue(pretty.contains("  "))
    }

    @Test fun regexDigits() {
        assertEquals(listOf("1", "22"), TextData.regexTest("\\d+", "a1b22"))
    }

    @Test fun urlRoundTrip() {
        val original = "a b&c"
        assertEquals(original, TextData.urlDecode(TextData.urlEncode(original)))
        assertTrue(TextData.urlEncode("a b").contains("%20"))
    }

    @Test fun caesarKnownAndRoundTrip() {
        assertEquals("Khoor, Zruog! abc ABC", TextData.caesar("Hello, World! xyz XYZ", 3, true))
        val original = "Hello, World! xyz XYZ 123"
        assertEquals(original, TextData.caesar(TextData.caesar(original, 5, true), 5, false))
    }

    @Test fun xorRoundTrip() {
        val text = "hello, CalcU!"
        val key = "s3cret"
        val hex = TextData.xorHex(text, key)
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val kb = key.toByteArray(Charsets.UTF_8)
        val back = bytes.mapIndexed { i, b -> (b.toInt() xor kb[i % kb.size].toInt()).toByte() }
            .toByteArray().toString(Charsets.UTF_8)
        assertEquals(text, back)
    }

    @Test fun xorEmptyKeyRejected() {
        try {
            TextData.xorHex("abc", "")
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test fun badInputsThrowIAEOnly() {
        try {
            TextData.base64Decode("!!!")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.qrMatrix("abc", 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.qrMatrix("", 200)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.morseEncode("~")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.binaryToText("zzzz")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.jsonPretty("{\"a\":")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.unixToDate(Long.MAX_VALUE)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals("", TextData.morseEncode("   "))
        assertEquals("", TextData.binaryToText("   "))
    }

    @Test fun crashHardeningEdges() {
        assertEquals("", TextData.base64Decode(TextData.base64Encode("")))
        assertEquals(0, TextData.wordCount("   "))
        assertEquals(0, TextData.lineCount(""))
        assertEquals("   ", TextData.titleCase("   "))
        assertEquals("", TextData.textToBinary(""))
        assertEquals("", TextData.textToHex(""))
        assertEquals("abc", TextData.caesar("abc", 0, true))
        assertEquals("", TextData.xorHex("", "key"))
        try {
            TextData.qrMatrix("abc", 5000)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.qrMatrix("x".repeat(6000), 200)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.morseDecode(".....---")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.morseDecode("... --- ... / ???")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.regexTest("[", "abc")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.urlDecode("%ZZ")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.jsonPretty("{]")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            TextData.jsonPretty("\"abc")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun rot13RoundTrip() {
        val original = "Hello, World! 123"
        assertEquals(original, TextData.rot13(TextData.rot13(original)))
        assertEquals("Uryyb", TextData.rot13("Hello"))
    }

    @Test fun slugBasic() {
        assertEquals("hello-world", TextData.slugify("Hello, World!"))
    }

    @Test fun palindromeClassic() {
        assertTrue(TextData.isPalindrome("A man a plan a canal Panama"))
        assertFalse(TextData.isPalindrome("hello"))
    }

    @Test fun emailsExtractTwo() {
        val found = TextData.extractEmails("a@b.com and x.y+z@sub.domain.org ok")
        assertEquals(2, found.size)
        assertTrue(found.contains("a@b.com"))
    }

    @Test fun diffBasic() {
        val d = TextData.diffLines("a\nb\nc", "a\nx\nc")
        assertTrue(d.contains("- b"))
        assertTrue(d.contains("+ x"))
        assertTrue(d.contains("  a"))
    }

    @Test fun csvRoundTrip() {
        val csv = "a,b\n1,2\n3,4"
        val json = TextData.csvToJson(csv)
        assertTrue(json.contains("\"a\""))
        assertEquals(csv, TextData.jsonToCsv(json))
    }

    @Test fun csvQuoteAware() {
        val json = TextData.csvToJson("a,b\n\"x,y\",2")
        assertTrue(json.contains("x,y"))
        assertEquals("a,b\n\"x,y\",2", TextData.jsonToCsv(json))
    }

    @Test fun cronQuarterHour() {
        val text = TextData.crontabExplain("*/15 * * * *").lowercase()
        assertTrue(text.contains("15"))
        assertTrue(text.contains("minute"))
    }

    @Test fun leapYears() {
        assertTrue(TextData.isLeapYear(2024))
        assertFalse(TextData.isLeapYear(2025))
        assertFalse(TextData.isLeapYear(1900))
        assertTrue(TextData.isLeapYear(2000))
    }

    @Test fun discordFormat() {
        assertEquals("<t:0:R>", TextData.discordTimestamp(0L, "R"))
    }

    @Test fun jsonSortAndCompare() {
        assertEquals("{\"a\":1,\"b\":2}", TextData.jsonSortKeys("{\"b\":2,\"a\":1}"))
        assertEquals(listOf("a"), TextData.jsonCompare("{\"a\":1}", "{\"a\":2}"))
        assertTrue(TextData.jsonCompare("{\"a\":1}", "{\"a\":1}").isEmpty())
    }

    @Test fun transposeBasic() {
        assertEquals("a,1\nb,2", TextData.transposeCsv("a,b\n1,2"))
    }
}
