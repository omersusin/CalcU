package calc.u

import calc.u.core.GapTools
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class GapToolsTest {

    private fun iae(block: () -> Unit) {
        try {
            block()
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun aquariumFullAndPartial() {
        assertEquals(100.0, GapTools.aquariumVolumeLiters(100.0, 50.0, 20.0), 1e-9)
        assertEquals(50.0, GapTools.aquariumVolumeLiters(100.0, 50.0, 20.0, 0.5), 1e-9)
        iae { GapTools.aquariumVolumeLiters(0.0, 50.0, 20.0) }
        iae { GapTools.aquariumVolumeLiters(100.0, -1.0, 20.0) }
        iae { GapTools.aquariumVolumeLiters(100.0, 50.0, 20.0, 0.0) }
        iae { GapTools.aquariumVolumeLiters(100.0, 50.0, 20.0, 1.5) }
        iae { GapTools.aquariumVolumeLiters(Double.NaN, 50.0, 20.0) }
    }

    @Test fun bsaDuBoisValue() {
        assertEquals(1.846, GapTools.bsaDuBois(70.0, 175.0), 0.01)
        iae { GapTools.bsaDuBois(0.0, 175.0) }
        iae { GapTools.bsaDuBois(70.0, 0.0) }
    }

    @Test fun whrRatioAndCategory() {
        assertEquals(85.0 / 95.0, GapTools.waistHipRatio(85.0, 95.0), 1e-9)
        assertEquals("typical range", GapTools.waistHipCategory(0.85, true))
        assertEquals("above typical cut-point", GapTools.waistHipCategory(0.95, true))
        assertEquals("typical range", GapTools.waistHipCategory(0.80, false))
        assertEquals("above typical cut-point", GapTools.waistHipCategory(0.90, false))
        iae { GapTools.waistHipRatio(0.0, 95.0) }
        iae { GapTools.waistHipRatio(85.0, 0.0) }
        iae { GapTools.waistHipCategory(0.0, true) }
    }

    @Test fun lbmHumeValues() {
        assertEquals(52.81, GapTools.lbmHume(70.0, 175.0, true), 0.05)
        assertEquals(50.58, GapTools.lbmHume(70.0, 175.0, false), 0.05)
        iae { GapTools.lbmHume(0.0, 175.0, true) }
        iae { GapTools.lbmHume(70.0, 0.0, false) }
    }

    @Test fun rfmValues() {
        assertEquals(22.82, GapTools.rfm(175.0, 85.0, true), 0.05)
        assertEquals(34.82, GapTools.rfm(175.0, 85.0, false), 0.05)
        iae { GapTools.rfm(0.0, 85.0, true) }
        iae { GapTools.rfm(175.0, 0.0, true) }
    }

    @Test fun bmrBothEquations() {
        assertEquals(1648.75, GapTools.bmrMifflin(70.0, 175.0, 30, true), 1e-6)
        assertEquals(1482.75, GapTools.bmrMifflin(70.0, 175.0, 30, false), 1e-6)
        assertEquals(1695.67, GapTools.bmrHarrisBenedict(70.0, 175.0, 30, true), 0.05)
        assertEquals(1507.13, GapTools.bmrHarrisBenedict(70.0, 175.0, 30, false), 0.05)
        iae { GapTools.bmrMifflin(0.0, 175.0, 30, true) }
        iae { GapTools.bmrMifflin(70.0, 175.0, 200, true) }
        iae { GapTools.bmrHarrisBenedict(70.0, 0.0, 30, false) }
        iae { GapTools.bmrHarrisBenedict(70.0, 175.0, -1, false) }
    }

    @Test fun bloodPressureCategories() {
        assertEquals("normal range", GapTools.bloodPressureCategory(110, 70))
        assertEquals("elevated range", GapTools.bloodPressureCategory(125, 75))
        assertEquals("stage 1 range", GapTools.bloodPressureCategory(135, 85))
        assertEquals("stage 1 range", GapTools.bloodPressureCategory(125, 85))
        assertEquals("stage 2 range", GapTools.bloodPressureCategory(145, 95))
        assertEquals("hypertensive crisis range", GapTools.bloodPressureCategory(185, 110))
        assertEquals("hypertensive crisis range", GapTools.bloodPressureCategory(150, 125))
        iae { GapTools.bloodPressureCategory(10, 70) }
        iae { GapTools.bloodPressureCategory(120, 10) }
    }

    @Test fun qtcBazettValues() {
        assertEquals(400.0, GapTools.qtcBazett(400.0, 1.0), 1e-9)
        assertEquals(400.0, GapTools.qtcFromHeartRate(400.0, 60.0), 1e-9)
        assertEquals(400.0 / kotlin.math.sqrt(0.5), GapTools.qtcBazett(400.0, 0.5), 1e-9)
        iae { GapTools.qtcBazett(0.0, 1.0) }
        iae { GapTools.qtcBazett(400.0, 0.0) }
        iae { GapTools.qtcFromHeartRate(400.0, 0.0) }
    }

    @Test fun homaIrValue() {
        assertEquals(90.0 * 10.0 / 405.0, GapTools.homaIr(90.0, 10.0), 1e-9)
        iae { GapTools.homaIr(0.0, 10.0) }
        iae { GapTools.homaIr(90.0, 0.0) }
    }

    @Test fun anionGapValues() {
        assertEquals(11.0, GapTools.anionGap(140.0, 105.0, 24.0), 1e-9)
        assertEquals(15.5, GapTools.anionGap(140.0, 105.0, 24.0, 4.5), 1e-9)
        iae { GapTools.anionGap(Double.NaN, 105.0, 24.0) }
        iae { GapTools.anionGap(140.0, 105.0, 24.0, -1.0) }
    }

    @Test fun correctedCalciumValue() {
        assertEquals(9.8, GapTools.correctedCalcium(9.0, 3.0), 1e-9)
        assertEquals(9.0, GapTools.correctedCalcium(9.0, 4.0), 1e-9)
        iae { GapTools.correctedCalcium(0.0, 4.0) }
        iae { GapTools.correctedCalcium(9.0, 0.0) }
    }

    @Test fun ldlFriedewaldValue() {
        assertEquals(120.0, GapTools.ldlFriedewald(200.0, 50.0, 150.0), 1e-9)
        iae { GapTools.ldlFriedewald(200.0, 50.0, 400.0) }
        iae { GapTools.ldlFriedewald(200.0, 50.0, 500.0) }
        iae { GapTools.ldlFriedewald(0.0, 50.0, 150.0) }
        iae { GapTools.ldlFriedewald(200.0, 0.0, 150.0) }
        iae { GapTools.ldlFriedewald(200.0, 50.0, 0.0) }
    }
}
