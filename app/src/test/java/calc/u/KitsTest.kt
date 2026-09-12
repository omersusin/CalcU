package calc.u

import calc.u.core.ClockAngle
import calc.u.core.ColorKit
import calc.u.core.Geometry
import calc.u.core.ScreenKit
import org.junit.Assert.assertEquals
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
}
