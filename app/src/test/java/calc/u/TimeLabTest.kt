package calc.u

import calc.u.core.TimeLab
import org.junit.Assert.*
import org.junit.Test

class TimeLabTest {
    @Test fun formatGolden() {
        assertEquals("1:02:03.45", TimeLab.formatHMS(3723456L))
    }

    @Test fun formatZero() {
        assertEquals("0:00:00.00", TimeLab.formatHMS(0L))
    }

    @Test fun lapsSplits() {
        val laps = TimeLab.addLap(listOf(1000L, 3000L), 6000L)
        assertEquals(3, laps.size)
        assertEquals(TimeLab.Lap(1, 1000L, 1000L), laps[0])
        assertEquals(TimeLab.Lap(2, 3000L, 2000L), laps[1])
        assertEquals(TimeLab.Lap(3, 6000L, 3000L), laps[2])
    }

    @Test fun lapsFirst() {
        val laps = TimeLab.addLap(emptyList(), 500L)
        assertEquals(1, laps.size)
        assertEquals(TimeLab.Lap(1, 500L, 500L), laps[0])
    }

    @Test fun pomoRounds() {
        val cfg = TimeLab.PomoConfig()
        assertEquals(4, cfg.roundsUntilLong)
        assertEquals("short", TimeLab.pomoPhase(3, cfg))
        assertEquals("long", TimeLab.pomoPhase(4, cfg))
    }

    @Test fun pomoStartIsFocus() {
        assertEquals("focus", TimeLab.pomoPhase(0, TimeLab.PomoConfig()))
    }

    @Test fun countdownGolden() {
        assertEquals(Triple(1L, 30L, 0L), TimeLab.countdownParts(90000L))
    }

    @Test fun formatHMSZeroExplicit() {
        assertEquals("0:00:00.00", TimeLab.formatHMS(0L))
    }

    @Test fun countdownParts61050() {
        assertEquals(Triple(1L, 1L, 5L), TimeLab.countdownParts(61050L))
    }

    @Test fun negativeClampedToZero() {
        assertEquals("0:00:00.00", TimeLab.formatHMS(-1000L))
        assertEquals(Triple(0L, 0L, 0L), TimeLab.countdownParts(-500L))
        assertEquals("focus", TimeLab.pomoPhase(0, TimeLab.PomoConfig(roundsUntilLong = 0)))
        assertEquals("long", TimeLab.pomoPhase(4, TimeLab.PomoConfig(roundsUntilLong = -2)))
    }

    @Test fun crashHardeningEdges() {
        assertEquals("0:00:00.00", TimeLab.formatHMS(Long.MIN_VALUE))
        assertEquals(Triple(0L, 0L, 0L), TimeLab.countdownParts(Long.MIN_VALUE))
        assertEquals("0:00:00.00", TimeLab.formatHMS(-1L))
        val laps = TimeLab.addLap(emptyList(), -500L)
        assertEquals(1, laps.size)
        assertEquals(TimeLab.Lap(1, -500L, -500L), laps[0])
        assertEquals("focus", TimeLab.pomoPhase(-3))
        assertEquals("short", TimeLab.pomoPhase(1, TimeLab.PomoConfig(roundsUntilLong = 0)))
        assertEquals(Triple(0L, 0L, 0L), TimeLab.countdownParts(5L))
    }
}
