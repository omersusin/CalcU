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
}
