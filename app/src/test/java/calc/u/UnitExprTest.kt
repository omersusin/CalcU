package calc.u

import calc.u.core.UnitExpr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class UnitExprTest {
    @Test fun ftLbfToNewtonMeter() {
        assertEquals(1.3558179483314004, UnitExpr.convertExpr(1.0, "ft*lbf", "N*m"), 1e-4)
    }

    @Test fun miPerHrToMeterPerSec() {
        assertEquals(0.44704, UnitExpr.convertExpr(1.0, "mi/hr", "m/s"), 1e-9)
    }

    @Test fun accelIdentityAndNewtonDecomposition() {
        assertEquals(9.8, UnitExpr.convertExpr(9.8, "m/s^2", "m/s^2"), 1e-9)
        assertEquals(1.0, UnitExpr.convertExpr(1.0, "kg*(m/s^2)", "N"), 1e-9)
    }

    @Test fun incompatibleThrows() {
        try {
            UnitExpr.convertExpr(1.0, "ft", "s")
            fail("expected incompatible dimensions to throw")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("incompatible: length vs time"))
        }
    }

    @Test fun newtonMeterOverJouleIsOne() {
        assertEquals(1.0, UnitExpr.convertExpr(1.0, "N*m", "J"), 1e-9)
        assertEquals(1.0, UnitExpr.convertExpr(1.0, "(N*m)/(J)", "1"), 1e-9)
    }

    @Test fun kWhToJoule() {
        assertEquals(3600000.0, UnitExpr.convertExpr(1.0, "kWh", "J"), 1e-6)
    }

    @Test fun psiToPascal() {
        assertEquals(6894.76, UnitExpr.convertExpr(1.0, "psi", "Pa"), 0.01)
    }

    @Test fun galToLiter() {
        assertEquals(3.78541, UnitExpr.convertExpr(1.0, "gal", "L"), 1e-4)
    }

    @Test fun degToRad() {
        assertEquals(Math.PI / 180.0, UnitExpr.convertExpr(1.0, "deg", "rad"), 1e-12)
    }

    @Test fun massVsLengthThrows() {
        try {
            UnitExpr.convertExpr(1.0, "kg", "m")
            fail("expected incompatible dimensions to throw")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("incompatible: mass vs length"))
        }
    }
}
