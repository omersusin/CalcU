package calc.u

import calc.u.data.decodeVariables
import calc.u.data.encodeVariables
import org.junit.Assert.*
import org.junit.Test

class VariablesPrefsTest {
    @Test fun roundTrip() {
        val vars = mapOf("x" to "2.5", "radius" to "12.5663706144")
        assertEquals(vars, decodeVariables(encodeVariables(vars)))
    }
    @Test fun emptyAndMalformed() {
        assertEquals(emptyMap<String, String>(), decodeVariables(""))
        assertEquals(emptyMap<String, String>(), decodeVariables(";;;"))
        assertEquals(mapOf("a" to "1"), decodeVariables("junk;a=1;=x"))
    }
    @Test fun encodedIsCapped() {
        val big = (1..200).associate { "v$it" to "123456789.123456789" }
        assertTrue(encodeVariables(big).length <= 4000)
    }
}
