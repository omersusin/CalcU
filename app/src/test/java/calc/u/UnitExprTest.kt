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

    @Test fun chainToYards() {
        assertEquals(22.0, UnitExpr.convertExpr(1.0, "chain", "yd"), 1e-9)
        assertEquals(22.0, UnitExpr.convertExpr(1.0, "chains", "yd"), 1e-9)
    }

    @Test fun surveyLengthRoundTrips() {
        assertEquals(4.0, UnitExpr.convertExpr(1.0, "hand", "in"), 1e-9)
        assertEquals(0.001, UnitExpr.convertExpr(1.0, "mil", "in"), 1e-12)
        assertEquals(0.01, UnitExpr.convertExpr(1.0, "link", "chain"), 1e-12)
        assertEquals(5.5, UnitExpr.convertExpr(1.0, "rod", "yd"), 1e-9)
        assertEquals(2.0, UnitExpr.convertExpr(1.0, "fathom", "yd"), 1e-9)
    }

    @Test fun fortnightToDays() {
        assertEquals(14.0, UnitExpr.convertExpr(1.0, "fortnight", "day"), 1e-9)
        assertEquals(14.0, UnitExpr.convertExpr(1.0, "fortnights", "days"), 1e-9)
        assertEquals(7.0, UnitExpr.convertExpr(1.0, "week", "day"), 1e-9)
        assertEquals(365.242198781, UnitExpr.convertExpr(1.0, "year", "day"), 1e-6)
    }

    @Test fun pointToInch() {
        assertEquals(1.0 / 72.0, UnitExpr.convertExpr(1.0, "point", "in"), 1e-12)
        assertEquals(12.0, UnitExpr.convertExpr(1.0, "pica", "point"), 1e-9)
    }

    @Test fun quartPintFluidOunce() {
        assertEquals(0.946352946, UnitExpr.convertExpr(1.0, "qt", "L"), 1e-9)
        assertEquals(2.0, UnitExpr.convertExpr(1.0, "quarts", "pt"), 1e-9)
        assertEquals(29.5735, UnitExpr.convertExpr(1.0, "fl oz", "mL"), 1e-4)
        assertEquals(29.5735, UnitExpr.convertExpr(1.0, "fluid ounce", "mL"), 1e-4)
        assertEquals(0.125, UnitExpr.convertExpr(1.0, "fldram", "floz"), 1e-9)
    }

    @Test fun ksiInHgTorr() {
        assertEquals(1000.0, UnitExpr.convertExpr(1.0, "ksi", "psi"), 1e-6)
        assertEquals(3386.389, UnitExpr.convertExpr(1.0, "inHg", "Pa"), 1e-3)
        assertEquals(3386.389, UnitExpr.convertExpr(1.0, "in hg", "Pa"), 1e-3)
        assertEquals(133.322368, UnitExpr.convertExpr(1.0, "torr", "Pa"), 1e-6)
    }

    @Test fun btuThermTonTnt() {
        assertEquals(1055.05585262, UnitExpr.convertExpr(1.0, "Btu", "J"), 1e-6)
        assertEquals(1e5, UnitExpr.convertExpr(1.0, "therm", "Btu"), 1e-3)
        assertEquals(4.184e9, UnitExpr.convertExpr(1.0, "tonTNT", "J"), 1e3)
        assertEquals(4.184e9, UnitExpr.convertExpr(1.0, "ton tnt", "J"), 1e3)
    }

    @Test fun knotAndTurn() {
        assertEquals(1852.0 / 3600.0, UnitExpr.convertExpr(1.0, "knot", "m/s"), 1e-12)
        assertEquals(1852.0 / 3600.0, UnitExpr.convertExpr(1.0, "kt", "m/s"), 1e-12)
        assertEquals(2 * Math.PI, UnitExpr.convertExpr(1.0, "turn", "rad"), 1e-12)
        assertEquals(2 * Math.PI, UnitExpr.convertExpr(1.0, "rev", "rad"), 1e-12)
        assertEquals(1.0, UnitExpr.convertExpr(1.0, "revolution", "turn"), 1e-12)
        assertEquals(1.0 / 60.0, UnitExpr.convertExpr(1.0, "arcmin", "deg"), 1e-12)
    }

    @Test fun emptyExpressionThrows() {
        assertEquals(1.0, UnitExpr.evaluate("").factor, 0.0)
        assertEquals(1.0, UnitExpr.evaluate("   ").factor, 0.0)
        try {
            UnitExpr.evaluate("/")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.evaluate("(")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun unknownUnitThrows() {
        try {
            UnitExpr.convertExpr(1.0, "furlongg", "m")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.evaluate("!!")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun hugeExponentOutOfRangeThrows() {
        try {
            UnitExpr.evaluate("m^99999999999999999999")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.evaluate("m^2.5")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun megaAuMonthRankine() {
        assertEquals(1.495978707e11, UnitExpr.convertExpr(1.0, "au", "m"), 1e5)
        assertEquals(2629743.8312232, UnitExpr.convertExpr(1.0, "month", "s"), 1e-3)
        assertEquals(273.15, UnitExpr.convertExpr(491.67, "R", "K"), 1e-9)
    }

    @Test fun megaRpmKibPpm() {
        assertEquals(0.10471975511965977, UnitExpr.convertExpr(1.0, "rpm", "rad/s"), 1e-12)
        assertEquals(6.283185307179586, UnitExpr.convertExpr(1.0, "rps", "rad/s"), 1e-12)
        assertEquals(1024.0, UnitExpr.convertExpr(1.0, "KiB", "B"), 1e-9)
        assertEquals(1e-6, UnitExpr.convertExpr(1.0, "ppm", "1"), 1e-12)
    }

    @Test fun megaCurieEvQuadrant() {
        assertEquals(3.7e10, UnitExpr.convertExpr(1.0, "Ci", "Bq"), 1e3)
        assertEquals(1.602176634e-19, UnitExpr.convertExpr(1.0, "eV", "J"), 1e-28)
        assertEquals(90.0, UnitExpr.convertExpr(1.0, "quadrant", "deg"), 1e-9)
        assertEquals(340.29, UnitExpr.convertExpr(1.0, "mach", "m/s"), 1e-9)
    }

    @Test fun megaElectricalFlow() {
        assertEquals(1000.0, UnitExpr.convertExpr(1.0, "kV", "V"), 1e-9)
        assertEquals(3600.0, UnitExpr.convertExpr(1.0, "Ah", "C"), 1e-9)
        assertEquals(1e9, UnitExpr.convertExpr(1.0, "GHz", "Hz"), 1e0)
        assertEquals(12.566370614359172, UnitExpr.convertExpr(1.0, "sphere", "sr"), 1e-9)
    }

    @Test fun crashHardeningEdges() {
        assertEquals(1.0, UnitExpr.evaluate("1").factor, 0.0)
        assertEquals(1.0, UnitExpr.evaluate("unitless").factor, 0.0)
        assertEquals(1.0, UnitExpr.evaluate("dimensionless").factor, 0.0)
        try {
            UnitExpr.evaluate("m/0")
            fail("expected AE")
        } catch (e: ArithmeticException) { }
        try {
            UnitExpr.evaluate("(m")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.evaluate("m)")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.evaluate("m*")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.convertExpr(1.0, "m", "")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.convertExpr(1.0, "", "m")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            UnitExpr.evaluate("bogusunit")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals(1000.0, UnitExpr.convertExpr(1.0, "km", "m"), 1e-9)
    }
}
