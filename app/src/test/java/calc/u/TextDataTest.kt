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
}
