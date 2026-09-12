package calc.u

import calc.u.core.Engine
import calc.u.core.NumberTheory
import calc.u.core.TextData
import org.junit.Assert.*
import org.junit.Test

class BountyCrashTest {
    @Test fun diffLinesLineCapThrows() {
        val big = (1..2001).joinToString("\n") { "line $it" }
        try {
            TextData.diffLines(big, "x")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("2000") == true)
        }
    }

    @Test fun diffLinesCharCapThrows() {
        val big = "a".repeat(200_001)
        try {
            TextData.diffLines(big, "")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("200000") == true)
        }
    }

    @Test fun diffLinesAtCapPasses() {
        val ok = (1..2000).joinToString("\n") { "line $it" }
        val out = TextData.diffLines(ok, ok)
        assertEquals(2000, out.size)
    }

    @Test fun diffLinesRespectsInterruption() {
        Thread.currentThread().interrupt()
        try {
            TextData.diffLines("a\nb\nc", "a\nx\nc")
            fail("expected cancellation")
        } catch (e: java.util.concurrent.CancellationException) {
        } finally {
            Thread.interrupted()
        }
    }

    @Test fun totientOverCapThrows() {
        try {
            NumberTheory.totient(1_000_000_000_001L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("1000000000000") == true)
        }
    }

    @Test fun primeFactorsOverCapThrows() {
        try {
            NumberTheory.primeFactors(1_000_000_000_001L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("1000000000000") == true)
        }
    }

    @Test fun trialDivisionAtCapWorks() {
        val factors = NumberTheory.primeFactors(1_000_000_000_000L)
        assertEquals(24, factors.size)
        assertEquals(1_000_000_000_000L, factors.fold(1L) { acc, f -> acc * f })
        assertEquals(400_000_000_000L, NumberTheory.totient(1_000_000_000_000L))
    }

    @Test fun validatorEvalAgreeTrailingPlus() {
        assertNull(Engine.validateExpr("5+"))
        assertEquals(5.0, Engine.eval("5+").getOrThrow().toDouble(), 0.0)
    }

    @Test fun validatorEvalAgreeUnclosedParen() {
        assertNull(Engine.validateExpr("((1+2"))
        assertEquals(3.0, Engine.eval("((1+2").getOrThrow().toDouble(), 0.0)
    }

    @Test fun validatorEvalAgreeDoubleTrailingPlus() {
        assertNull(Engine.validateExpr("5+3+"))
        assertEquals(8.0, Engine.eval("5+3+").getOrThrow().toDouble(), 0.0)
    }

    @Test fun divByZeroEvaluatesToClearError() {
        val result = Engine.eval("5/0")
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message
        assertTrue(msg?.isNotBlank() == true)
    }

    @Test fun extraCloseParenStillErrors() {
        assertEquals("Unbalanced brackets", Engine.validateExpr("2+3)"))
    }

    @Test fun farTooManyUnclosedParensErrors() {
        assertNotNull(Engine.validateExpr("(".repeat(1001) + "1"))
    }

    @Test fun decimalToFractionPrecisionCapThrows() {
        try {
            Engine.decimalToFraction("0.1234567890123")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("12") == true)
        }
    }

    @Test fun decimalToFractionAtCapWorks() {
        assertEquals("1/3", Engine.decimalToFraction("0.(3)"))
        assertNotNull(Engine.decimalToFraction("0.123456789012"))
    }

    @Test fun repeatingPeriodCapThrows() {
        try {
            Engine.decimalToFraction("0.(1234567890123)")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("12") == true)
        }
        try {
            Engine.repeatingToDecimal(1L, 97L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("12") == true)
        }
    }

    @Test fun repeatingToDecimalShortPeriodWorks() {
        assertEquals("0.(3)", Engine.repeatingToDecimal(1L, 3L))
    }

    @Test fun toFractionOverflowReturnsNull() {
        assertNull(Engine.toFraction(1e308, 1000))
    }

    @Test fun linear2x2NearSingularIsDashed() {
        assertEquals(Pair("—", "—"), Engine.solveLinearSystem2x2(1.0, 1.0, 2.0, 1.0 + 1e-13, 1.0, 2.0))
    }

    @Test fun linear2x2ExactSingularIsDashed() {
        assertEquals(Pair("—", "—"), Engine.solveLinearSystem2x2(1.0, 2.0, 3.0, 2.0, 4.0, 6.0))
    }

    @Test fun linear2x2NormalStillSolves() {
        val (x, y) = Engine.solveLinearSystem2x2(2.0, 3.0, 8.0, 1.0, -1.0, 1.0)
        assertEquals(2.2, x.toDouble(), 1e-9)
        assertEquals(1.2, y.toDouble(), 1e-9)
    }

    @Test fun solve3x3NearSingularHasNoUniqueSolution() {
        val tiny = listOf(
            listOf(1e-7, 0.0, 0.0),
            listOf(0.0, 1e-7, 0.0),
            listOf(0.0, 0.0, 1e-7)
        )
        assertEquals(listOf("no unique solution"), Engine.solve3x3(tiny, listOf(1.0, 2.0, 3.0)))
    }

    @Test fun solve3x3IdentityStillSolves() {
        val id = listOf(listOf(1.0, 0.0, 0.0), listOf(0.0, 1.0, 0.0), listOf(0.0, 0.0, 1.0))
        assertEquals(listOf("1", "2", "3"), Engine.solve3x3(id, listOf(1.0, 2.0, 3.0)))
    }

    @Test fun derivativeScalesStepWithX() {
        assertEquals(2e6, Engine.derivative("x^2", 1e6), 2e6 * 1e-6)
    }

    @Test fun derivativeAcceptsUppercaseX() {
        assertEquals(6.0, Engine.derivative("X^2", 3.0, true), 1e-3)
    }

    @Test fun derivativeLowercaseStillWorks() {
        assertEquals(6.0, Engine.derivative("x^2", 3.0, true), 1e-3)
    }
}
