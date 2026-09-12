package calc.u

import calc.u.core.HealthPlus
import org.junit.Assert.*
import org.junit.Test

class HealthPlusTest {
    @Test fun waterIntakeBase() {
        assertEquals(2450.0, HealthPlus.waterIntakeMl(70.0, 0.0), 1e-9)
    }
    @Test fun runPaceFiveKm() {
        assertEquals(5.0, HealthPlus.runPace(5.0, 25.0), 1e-9)
    }
    @Test fun oneRepMaxEpley() {
        assertEquals(116.67, HealthPlus.oneRepMax(100.0, 5), 0.01)
    }
    @Test fun targetHeartRateZone() {
        assertEquals(133.0, HealthPlus.targetHeartRate(30, 70.0), 1e-9)
    }
}
