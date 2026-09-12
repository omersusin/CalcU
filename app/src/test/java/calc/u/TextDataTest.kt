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
}
