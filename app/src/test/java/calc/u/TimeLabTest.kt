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
}
