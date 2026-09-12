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
}
