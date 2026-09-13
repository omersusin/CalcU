package calc.u

import calc.u.core.TextCalc
import calc.u.core.TextSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextCalcTest {
    @Test fun variablesPersistAcrossLines() {
        val r = TextCalc.evaluateAll("price = 100\ntax = price * 0.1\nprice + tax")
        assertEquals(3, r.size)
        assertEquals("100", r[0].display)
        assertNull(r[0].error)
        assertEquals("10", r[1].display)
        assertEquals("110", r[2].display)
        assertNull(r[2].error)
    }

    @Test fun shadowingKeepsLatestValue() {
        val r = TextCalc.evaluateAll("x = 2\nx = x + 3\nx * 2")
        assertEquals("2", r[0].display)
        assertEquals("5", r[1].display)
        assertEquals("10", r[2].display)
    }

    @Test fun unknownVariableNamesTheIssue() {
        val r = TextCalc.evaluateAll("grand = price + 5")
        assertTrue(r[0].error?.contains("price") == true)
    }

    @Test fun reservedNameRejected() {
        val r = TextCalc.evaluateAll("sin = 3")
        assertTrue(r[0].error?.contains("reserved") == true)
    }

    @Test fun divisionByZeroIsSmart() {
        val r = TextCalc.evaluateAll("1/0")
        assertEquals("Division by zero", r[0].error)
    }

    @Test fun commentsAndBlanksHaveNoResult() {
        val r = TextCalc.evaluateAll("# hello\n\n2 + 2")
        assertEquals(3, r.size)
        assertEquals("", r[0].display)
        assertNull(r[0].error)
        assertEquals("", r[1].display)
        assertEquals("4", r[2].display)
    }

    @Test fun missingExpressionAfterEquals() {
        val r = TextCalc.evaluateAll("a = # note")
        assertTrue(r[0].error?.contains("Missing expression") == true)
    }

    @Test fun prevHasNoValueOnFirstLine() {
        val r = TextCalc.evaluateAll("prev + 1")
        assertTrue(r[0].error?.contains("prev") == true)
    }

    @Test fun sessionSerializeRoundTrip() {
        val sessions = listOf(
            TextSession("a", "x = 1", 1L),
            TextSession("b", "y = 2", 2L)
        )
        val back = TextCalc.decodeSessions(TextCalc.encodeSessions(sessions))
        assertEquals(sessions, back)
    }

    @Test fun decodeBadJsonGivesEmpty() {
        assertEquals(emptyList<TextSession>(), TextCalc.decodeSessions("nope"))
    }

    @Test fun renderWithResultsAppendsDisplays() {
        val out = TextCalc.renderWithResults("a = 2\n3 + 4")
        assertTrue(out.contains("a = 2 => 2"))
        assertTrue(out.contains("3 + 4 => 7"))
    }

    @Test fun tokenizeColorsNumbersAndComments() {
        val tokens = TextCalc.tokenize("a = 2 # note")
        assertTrue(tokens.any { it.kind == calc.u.core.TextTokenKind.Number })
        assertTrue(tokens.any { it.kind == calc.u.core.TextTokenKind.Comment })
    }
}
