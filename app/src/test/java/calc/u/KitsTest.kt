package calc.u

import calc.u.core.ClockAngle
import calc.u.core.ClockKit
import calc.u.core.ColorKit
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.ScreenKit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KitsTest {
    @Test fun triangle345Area6AndRightAngle() {
        val r = Geometry.solveTriangleSSS(3.0, 4.0, 5.0)
        assertEquals(6.0, r["area"]!!, 1e-9)
        assertEquals(12.0, r["perimeter"]!!, 1e-9)
        assertEquals(90.0, r["angleC"]!!, 1e-6)
    }

    @Test fun redHexRoundTrip() {
        val rgb = ColorKit.hexToRgb("#FF0000")
        assertEquals(Triple(255, 0, 0), rgb)
        assertEquals("#FF0000", ColorKit.rgbToHex(rgb.first, rgb.second, rgb.third))
    }

    @Test fun aspect1920x1080() {
        assertEquals("16:9", ScreenKit.aspectRatio(1920, 1080))
    }

    @Test fun clockNoonZero() {
        assertEquals(0.0, ClockAngle.angle(12, 0), 1e-9)
    }

    @Test fun healthInvalidReturnsNaN() {
        assertTrue(HealthDate.bmi(0.0, 175.0).isNaN())
        assertTrue(HealthDate.bmi(70.0, 0.0).isNaN())
        assertTrue(HealthDate.tdee(0.0, 175.0, 30, true, 1.55).isNaN())
        assertTrue(HealthDate.bodyFatNavy(80.0, 90.0, 175.0, 0.0, true).isNaN())
    }

    @Test fun clockInvalidDateThrowsIAE() {
        try {
            ClockKit.weekdayName(2026, 13, 40)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ClockKit.daysUntil(2026, 2, 30)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun triangleInvalidThrows() {
        try {
            Geometry.solveTriangleSSS(1.0, 2.0, 10.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ScreenKit.aspectRatio(0, 1080)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun crashHardeningEdges() {
        assertTrue(Geometry.circleArea(-5.0) >= 0.0)
        assertEquals(0.0, Geometry.circleArea(0.0), 0.0)
        try {
            Geometry.solveTriangleSSS(-1.0, 2.0, 2.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ColorKit.hexToRgb("")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ColorKit.hexToRgb("#GGGGGG")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ColorKit.rgbToHex(256, 0, 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ColorKit.rgbToHsl(-1, 0, 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ColorKit.hslToRgb(400.0, 0.5, 0.5)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals("#000000", ColorKit.rgbToHex(0, 0, 0))
        try {
            ScreenKit.aspectRatio(-1, 100)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            ScreenKit.ppi(1920, 1080, 0.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            calc.u.core.VectorKit.dot(listOf(1.0), listOf(1.0, 2.0))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            calc.u.core.VectorKit.cross(listOf(1.0, 2.0), listOf(1.0, 2.0, 3.0))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            calc.u.core.VectorKit.magnitude(emptyList())
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            calc.u.core.VectorKit.angleDeg(listOf(0.0, 0.0), listOf(1.0, 0.0))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            calc.u.core.PaintKit.paintLiters(10.0, 0, 10.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            calc.u.core.PaintKit.tilesNeeded(10.0, 0.0, 10.0, 5.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            calc.u.core.TripKit.tripTime(10.0, 0.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals("unknown zone", ClockKit.worldTime("Not_A_Zone"))
        try {
            calc.u.core.IdealWeight.devine(0.0, true)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }
}
