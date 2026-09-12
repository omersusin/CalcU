package calc.u.core

object HealthPlus {
    fun waterIntakeMl(weightKg: Double, activeMin: Double): Double {
        require(weightKg > 0.0) { "weightKg must be > 0" }
        require(activeMin >= 0.0) { "activeMin must be >= 0" }
        return weightKg * 35.0 + activeMin / 30.0 * 12.0
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

    fun targetHeartRate(age: Int, intensityPct: Double): Double {
        require(age > 0 && age < 220) { "age must be in 1..219" }
        require(intensityPct in 0.0..100.0) { "intensityPct must be in 0..100" }
        val fraction = if (intensityPct > 1.0) intensityPct / 100.0 else intensityPct
        return (220 - age) * fraction
    }
}
