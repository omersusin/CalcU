package calc.u

import calc.u.core.NumberTheory
import org.junit.Assert.*
import org.junit.Test

class NumberTheoryTest {
    @Test fun totientOfNine() {
        assertEquals(6L, NumberTheory.totient(9L))
    }
    @Test fun modInverseBasic() {
        assertEquals(4L, NumberTheory.modInverse(3L, 11L))
    }
    @Test fun primeFactors84() {
        assertEquals(listOf(2L, 2L, 3L, 7L), NumberTheory.primeFactors(84L))
    }
    @Test fun fibTen() {
        assertEquals(55L, NumberTheory.fibonacci(10))
    }
    @Test fun collatzSix() {
        assertEquals(8, NumberTheory.collatzSteps(6L))
    }
    @Test fun factorString84() {
        assertEquals("2²·3·7", NumberTheory.factorString(84L))
    }
    @Test fun factorString600() {
        assertEquals("2³·3·5²", NumberTheory.factorString(600L))
    }
    @Test fun factorStringPowerOfTwo() {
        assertEquals("2¹⁰", NumberTheory.factorString(1024L))
    }
    @Test fun factorStringPrime() {
        assertEquals("13", NumberTheory.factorString(13L))
    }

    @Test fun invalidInputsThrowIAE() {
        try {
            NumberTheory.totient(0L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.modInverse(2L, 4L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.primeFactors(1L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.fibonacci(93)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.collatzSteps(0L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals(listOf(2L), NumberTheory.primeFactors(2L))
    }

    @Test fun crashHardeningEdges() {
        assertEquals(1L, NumberTheory.totient(1L))
        assertEquals(0L, NumberTheory.fibonacci(0))
        assertEquals(7540113804746346429L, NumberTheory.fibonacci(92))
        assertEquals(0, NumberTheory.collatzSteps(1L))
        assertEquals(listOf(13L), NumberTheory.primeFactors(13L))
        try {
            NumberTheory.fibonacci(-1)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.modInverse(0L, 2L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.modInverse(3L, 1L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.totient(-5L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            NumberTheory.collatzSteps(-1L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }
}
