package calc.u

import calc.u.core.Currency
import calc.u.core.Engine
import calc.u.core.Finance
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.Units
import org.junit.Assert.*
import org.junit.Test

class EngineTest {
    @Test fun basic() {
        assertEquals("4", Engine.format(Engine.eval("2+2").getOrThrow()))
    }
    @Test fun sci() {
        assertNotNull(Engine.eval("sin(30)", true).getOrNull())
    }
    @Test fun fraction() {
        assertEquals(Pair(1L, 3L), Engine.toFraction(0.3333333, 100))
    }
    @Test fun prime() { assertTrue(Engine.isPrime(13)); assertFalse(Engine.isPrime(15)) }
    @Test fun quadratic() { assertEquals(2, Engine.solveQuadratic(1.0, -3.0, 2.0).size) }
    @Test fun units() {
        assertEquals(1000.0, Units.convert(1.0, Units.length["km"]!!, Units.length["m"]!!), 1e-9)
        assertEquals(32.0, Units.convertTemp(0.0, "C", "F"), 1e-9)
    }
    @Test fun finance() {
        assertEquals(88.0, Finance.emi(1000.0, 0.0, 12).let { 1000.0 / 12 }, 1e-9)
        assertTrue(Finance.compound(100.0, 10.0, 1.0) > 100)
    }
    @Test fun geometryHealth() {
        assertEquals(78.5, Geometry.circleArea(5.0), 0.1)
        assertTrue(HealthDate.bmi(70.0, 175.0) > 20)
    }
    @Test fun currencyCodes() { assertTrue(Currency.codes.size >= 60) }
}
