package calc.u

import calc.u.core.Electro
import org.junit.Assert.*
import org.junit.Test

class ElectroTest {
    @Test fun decode47k() {
        assertEquals("4.7kΩ ±5%", Electro.decode4Band(listOf("yellow", "violet", "red", "gold")))
    }
    @Test fun decode5Band() {
        assertEquals("10kΩ ±1%", Electro.decode5Band(listOf("brown", "black", "black", "red", "brown")))
    }
    @Test fun divider() {
        assertEquals(6.0, Electro.voltageDivider(12.0, 2000.0, 2000.0), 1e-9)
    }
    @Test fun led() {
        assertEquals(350.0, Electro.ledResistor(9.0, 2.0, 20.0), 1e-9)
    }
    @Test fun rc() {
        assertEquals(0.001, Electro.rcTimeConstant(1000.0, 1e-6), 1e-12)
    }

    @Test fun decode5Band10k() {
        assertEquals("10kΩ ±1%", Electro.decode5Band(listOf("brown", "black", "black", "red", "brown")))
    }

    @Test fun voltageDividerGuards() {
        try {
            Electro.voltageDivider(5.0, 0.0, 1000.0)
            fail("expected r1 <= 0 to throw")
        } catch (e: IllegalArgumentException) {
        }
        try {
            Electro.voltageDivider(5.0, 1000.0, -1.0)
            fail("expected r2 <= 0 to throw")
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test fun decodeInvalidColorsThrow() {
        try {
            Electro.decode4Band(listOf("red", "green"))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.decode4Band(listOf("pink", "black", "red", "gold"))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.decode5Band(listOf("brown", "black", "black", "red", "pink"))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun crashHardeningEdges() {
        assertEquals("4.7kΩ ±5%", Electro.decode4Band(listOf(" Yellow ", "VIOLET", "Red", "Gold")))
        try {
            Electro.decode4Band(listOf("yellow", "violet", "red", "pink"))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.decode5Band(listOf("gold", "black", "black", "red", "brown"))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.ledResistor(2.0, 2.0, 20.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.ledResistor(9.0, 2.0, 0.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.ledResistor(9.0, -1.0, 20.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.rcTimeConstant(0.0, 1e-6)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Electro.voltageDivider(Double.NaN, 1000.0, 1000.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }
}
