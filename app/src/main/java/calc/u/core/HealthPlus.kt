package calc.u.core

object HealthPlus {
    fun waterIntakeMl(weightKg: Double, activeMin: Double): Double {
        require(weightKg > 0.0) { "weightKg must be > 0" }
        require(activeMin >= 0.0) { "activeMin must be >= 0" }
        return weightKg * 35.0 + activeMin / 30.0 * 120.0
    }

    fun runPace(distanceKm: Double, minutes: Double): Double {
        require(distanceKm > 0.0) { "distanceKm must be > 0" }
        require(minutes >= 0.0) { "minutes must be >= 0" }
        return minutes / distanceKm
    }

    fun oneRepMax(weight: Double, reps: Int, formula: String = "epley"): Double {
        require(weight > 0.0) { "weight must be > 0" }
        require(reps > 0) { "reps must be > 0" }
        return when (formula.lowercase()) {
            "epley" -> weight * (1.0 + reps / 30.0)
            "brzycki" -> {
                require(reps < 37) { "reps must be < 37 for brzycki" }
                weight * 36.0 / (37 - reps)
            }
            else -> throw IllegalArgumentException("unknown formula: $formula")
        }
    }

    /**
     * Target heart rate in bpm. [intensityPct] is always a percent in 0..100
     * (e.g. 70 means 70%), never a 0..1 fraction.
     */
    fun targetHeartRate(age: Int, intensityPct: Double): Double {
        require(age > 0 && age < 220) { "age must be in 1..219" }
        require(intensityPct in 0.0..100.0) { "intensityPct must be in 0..100" }
        return (220 - age) * intensityPct / 100.0
    }

    // Kg to reach healthy BMI band: current minus 24.9 ceiling if above,
    // current minus 18.5 floor if below, else 0.0. Positive = excess to lose,
    // negative = deficit to gain.
    fun bmiDelta(weightKg: Double, heightCm: Double): Double {
        require(weightKg > 0.0) { "weightKg must be > 0" }
        require(heightCm > 0.0) { "heightCm must be > 0" }
        val hM = heightCm / 100.0
        val h2 = hM * hM
        val bmi = weightKg / h2
        return when {
            bmi > 24.9 -> weightKg - 24.9 * h2
            bmi < 18.5 -> weightKg - 18.5 * h2
            else -> 0.0
        }
    }
}
