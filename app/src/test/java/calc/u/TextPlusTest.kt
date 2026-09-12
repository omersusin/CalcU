package calc.u

import calc.u.core.TextPlus
import org.junit.Assert.*
import org.junit.Test

class TextPlusTest {
    @Test fun hiddenCharsFindsZeroWidth() {
        val found = TextPlus.hiddenChars("a\u200Bb")
        assertEquals(1, found.size)
        assertEquals(1, found[0].first)
        assertEquals("U+200B", found[0].third)
    }

    @Test fun unicodeRoundTrip() {
        assertEquals("\\u0041\\u0042", TextPlus.unicodeEncode("AB"))
        assertEquals("AB", TextPlus.unicodeDecode(TextPlus.unicodeEncode("AB")))
    }

    @Test fun urlParseAndBuild() {
        val original = "https://example.com/path?q=1#frag"
        val parts = TextPlus.urlParse(original)
        assertEquals("https", parts["protocol"])
        assertEquals("example.com", parts["host"])
        assertEquals("/path", parts["path"])
        assertEquals("q=1", parts["query"])
        assertEquals("frag", parts["fragment"])
        assertEquals(original, TextPlus.urlBuild(parts))
    }

    @Test fun censorMasksWholeWord() {
        assertEquals("Hello ███ world", TextPlus.censor("Hello bad world", listOf("bad")))
        assertEquals("███ man", TextPlus.censor("BAD man", listOf("bad")))
    }

    @Test fun substring1Based() {
        assertEquals("bcd", TextPlus.substring1("abcdef", 2, 3, false))
        assertEquals("bc\nfg", TextPlus.substring1("abcd\nefgh", 2, 2, true))
    }

    @Test fun dedupeAll() {
        assertEquals("a\nb\nc", TextPlus.dedupeLines("a\nb\na\nc", "all"))
    }

    @Test fun dedupeConsecutiveAndUnique() {
        assertEquals("a\nb", TextPlus.dedupeLines("a\na\nb", "consecutive"))
        assertEquals("b\nc", TextPlus.dedupeLines("a\nb\na\nc", "unique"))
    }

    @Test fun reverseTextBasic() {
        assertEquals("cba", TextPlus.reverseText("abc"))
    }

    @Test fun rotateTextNormalized() {
        assertEquals("cdefab", TextPlus.rotateText("abcdef", 2))
        assertEquals("fabcde", TextPlus.rotateText("abcdef", -1))
    }

    @Test fun quoteLinesSkipsQuoted() {
        assertEquals("> a\n> b", TextPlus.quoteLines("a\n> b", "> "))
    }

    @Test fun splitAndJoin() {
        assertEquals(listOf("a", "b", "c"), TextPlus.splitText("a,b,c", ","))
        assertEquals("a,b", TextPlus.joinLines(listOf("a", "b"), ","))
    }

    @Test fun repeatNegativeEmpty() {
        assertEquals("ababab", TextPlus.repeatText("ab", 3))
        assertEquals("", TextPlus.repeatText("ab", -1))
    }

    @Test fun truncateEnd() {
        assertEquals("abcd…", TextPlus.truncateText("abcdef", 5, "end", "…"))
    }

    @Test fun listSortCaseInsensitive() {
        assertEquals(listOf("A", "b", "c"), TextPlus.listSort(listOf("b", "A", "c")))
    }

    @Test fun listShuffleKeepsElements() {
        val shuffled = TextPlus.listShuffle(listOf("a", "b", "c"), 42L)
        assertEquals(3, shuffled.size)
        assertTrue(shuffled.containsAll(listOf("a", "b", "c")))
    }

    @Test fun listUniqueBasic() {
        assertEquals(listOf("a", "b"), TextPlus.listUnique(listOf("a", "b", "a")))
    }

    @Test fun listFrequencyCounts() {
        val freq = TextPlus.listFrequency(listOf("a", "b", "a"))
        assertEquals(2, freq["a"])
        assertEquals(1, freq["b"])
    }

    @Test fun listRotateLeft() {
        assertEquals(listOf("b", "c", "a"), TextPlus.listRotate(listOf("a", "b", "c"), 1))
    }

    @Test fun listChunkSize() {
        assertEquals(listOf(listOf("a", "b"), listOf("c")), TextPlus.listChunk(listOf("a", "b", "c"), 2))
    }

    @Test fun listDuplicateTwice() {
        assertEquals(listOf("a", "b", "a", "b"), TextPlus.listDuplicate(listOf("a", "b")))
    }

    @Test fun csvSwapColumnsBasic() {
        assertEquals("c,b,a\n3,2,1", TextPlus.csvSwapColumns("a,b,c\n1,2,3", 0, 2))
    }

    @Test fun csvTransposeShape() {
        assertEquals("a,c\nb,d", TextPlus.csvTranspose("a,b\nc,d"))
    }

    @Test fun csvChangeDelimiterBasic() {
        assertEquals("a;b\nc;d", TextPlus.csvChangeDelimiter("a,b\nc,d", ',', ';'))
    }

    @Test fun csvFindIncompleteRow() {
        assertEquals(listOf(1), TextPlus.csvFindIncomplete("a,b\n1\n2,3"))
    }

    @Test fun jsonMinifyStrips() {
        assertEquals("{\"a\":[1,2]}", TextPlus.jsonMinify("{ \"a\" : [1, 2] }"))
    }

    @Test fun jsonValidateTrueFalse() {
        assertTrue(TextPlus.jsonValidate("{\"a\":1}"))
        assertFalse(TextPlus.jsonValidate("{bad}"))
    }

    @Test fun jsonEscapeQuotes() {
        assertEquals("a\\\"b", TextPlus.jsonEscape("a\"b"))
    }

    @Test fun xmlPrettyIndented() {
        val pretty = TextPlus.xmlPretty("<a><b>x</b></a>")
        assertTrue(pretty.contains("\n"))
        assertTrue(pretty.contains("  "))
    }

    @Test fun xmlValidateTrueFalse() {
        assertTrue(TextPlus.xmlValidate("<a></a>"))
        assertFalse(TextPlus.xmlValidate("<a>"))
    }

    @Test fun caseConversions() {
        assertEquals("hello_world", TextPlus.caseSnake("HelloWorld"))
        assertEquals("hello-world", TextPlus.caseKebab("HelloWorld"))
        assertEquals("helloWorld", TextPlus.caseCamel("hello_world"))
        assertEquals("HELLO_WORLD", TextPlus.caseConstant("hello-world"))
    }
}
