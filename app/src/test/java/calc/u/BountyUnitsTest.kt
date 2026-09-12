package calc.u

import calc.u.core.UnitExpr
import calc.u.core.Units
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BountyUnitsTest {
    @Test fun exactMMIsMegameter() {
        assertEquals(1e6, UnitExpr.convertExpr(1.0, "MM", "m"), 1e-3)
    }

    @Test fun exactMMIsNeverMillimeter() {
        assertEquals(1e9, UnitExpr.convertExpr(1.0, "MM", "mm"), 1e3)
    }

    @Test fun exactMSIsMegasecond() {
        assertEquals(1e6, UnitExpr.convertExpr(1.0, "MS", "s"), 1e-3)
    }

    @Test fun exactPAIsPetaampere() {
        assertEquals(1e15, UnitExpr.convertExpr(1.0, "PA", "A"), 1e6)
    }

    @Test fun teslaAndGauss() {
        assertEquals(1e-4, UnitExpr.convertExpr(1.0, "G", "T"), 1e-12)
        assertEquals(10000.0, UnitExpr.convertExpr(1.0, "T", "G"), 1e-6)
    }

    @Test fun lowerMmStaysMillimeter() {
        assertEquals(0.001, UnitExpr.convertExpr(1.0, "mm", "m"), 1e-12)
    }

    @Test fun cToKUsesCelsius() {
        assertEquals(373.15, UnitExpr.convertExpr(100.0, "C", "K"), 1e-9)
    }

    @Test fun fToCUsesFahrenheit() {
        assertEquals(100.0, UnitExpr.convertExpr(212.0, "F", "C"), 1e-9)
    }

    @Test fun coulombMismatchNamesCelsius() {
        try {
            UnitExpr.convertExpr(1.0, "C", "J")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message.orEmpty().contains("Celsius"))
        }
    }

    @Test fun shoeUsM10Is27cm() {
        assertEquals(27.09, Units.shoeUsMToCm(10.0), 0.5)
        assertEquals(27.09, Units.convertShoe(10.0, "US_M", "CM"), 0.5)
    }

    @Test fun shoeEu42Is27cm() {
        assertEquals(26.67, Units.shoeEuToCm(42.0), 0.5)
    }

    @Test fun shoeConvertRoutesAffine() {
        val cm = Units.convert(10.0, Units.shoe.getValue("US_M"), Units.shoe.getValue("CM"))
        assertEquals(Units.shoeUsMToCm(10.0), cm, 1e-9)
        assertEquals(10.0, Units.convert(cm, Units.shoe.getValue("CM"), Units.shoe.getValue("US_M")), 1e-9)
    }

    @Test fun ringUs10IsEu62() {
        assertEquals(62.1, Units.ringUsToEu(10.0), 0.5)
        assertEquals(10.0, Units.ringEuToUs(62.1), 1e-9)
    }

    @Test fun ringConvertRoundTrip() {
        val eu = Units.convert(10.0, Units.ring.getValue("US"), Units.ring.getValue("EU"))
        assertEquals(10.0, Units.convert(eu, Units.ring.getValue("EU"), Units.ring.getValue("US")), 1e-9)
    }

    @Test fun ebIsBinary() {
        assertEquals(1152921504606846976.0, Units.data.getValue("EB").toBase, 1.0)
    }

    @Test fun cupsStayLabeled() {
        assertEquals(0.24, Units.volume.getValue("cup").toBase, 1e-12)
        assertEquals(236.5882365, Units.cooking.getValue("cup").toBase, 1e-9)
    }

    @Test fun factorSyncs() {
        assertEquals(0.09290304, Units.area.getValue("ft2").toBase, 1e-12)
        assertEquals(4046.8564224, Units.area.getValue("acre").toBase, 1e-9)
        assertEquals(6894.757293168, Units.pressure.getValue("psi").toBase, 1e-9)
        assertEquals(133.322387415, Units.pressure.getValue("mmHg").toBase, 1e-9)
        assertEquals(133.322368, Units.pressure.getValue("torr").toBase, 1e-9)
        assertEquals(3.785411784, Units.volume.getValue("gal").toBase, 1e-12)
        assertEquals(0.00492892159375, Units.volume.getValue("tsp").toBase, 1e-15)
        assertEquals(0.01478676478125, Units.volume.getValue("tbsp").toBase, 1e-15)
        assertEquals(0.0295735295625, Units.volume.getValue("fl-oz").toBase, 1e-15)
        assertEquals(745.6998715822702, Units.power.getValue("hp").toBase, 1e-9)
    }

    @Test fun techAtAliasesKgfcm2() {
        assertEquals(Units.pressure.getValue("kgfcm2").toBase, Units.pressure.getValue("at").toBase, 0.0)
        assertEquals(98066.5, Units.pressure.getValue("at").toBase, 1e-9)
    }

    @Test fun meterOfWaterAndPsf() {
        assertEquals(9806.65, Units.pressure.getValue("mH2O").toBase, 1e-9)
        assertEquals(6894.757293168 / 144.0, Units.pressure.getValue("psf").toBase, 1e-9)
    }

    @Test fun unitExprAtWaterPsf() {
        assertEquals(98066.5, UnitExpr.convertExpr(1.0, "at", "Pa"), 1e-6)
        assertEquals(9806.65, UnitExpr.convertExpr(1.0, "mH2O", "Pa"), 1e-6)
        assertEquals(47.8802589800556, UnitExpr.convertExpr(1.0, "psf", "Pa"), 1e-6)
    }
}
