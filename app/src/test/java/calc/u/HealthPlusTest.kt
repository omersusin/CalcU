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
    @Test fun bodyFatDeurenberg() {
        assertEquals(18.1286, HealthPlus.bodyFatDeurenberg(70.0, 175.0, 30, true), 0.01)
        assertEquals(28.9286, HealthPlus.bodyFatDeurenberg(70.0, 175.0, 30, false), 0.01)
    }
    @Test fun bodyFatDeurenbergInvalid() {
        try {
            HealthPlus.bodyFatDeurenberg(0.0, 175.0, 30, true)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.bodyFatDeurenberg(70.0, 0.0, 30, true)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.bodyFatDeurenberg(70.0, 175.0, 150, true)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun invalidInputsThrowIAE() {
        try {
            HealthPlus.waterIntakeMl(0.0, 0.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.runPace(0.0, 25.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.oneRepMax(100.0, 5, "bogus")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.targetHeartRate(0, 70.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun crashHardeningEdges() {
        assertEquals(100.0, HealthPlus.oneRepMax(100.0, 1, "brzycki"), 1e-9)
        try {
            HealthPlus.oneRepMax(100.0, 37, "brzycki")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.oneRepMax(100.0, 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.oneRepMax(-100.0, 5)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.targetHeartRate(30, 101.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.targetHeartRate(220, 50.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.waterIntakeMl(-70.0, 0.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.waterIntakeMl(70.0, -1.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            HealthPlus.runPace(5.0, -1.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }
}
