package calc.u.core

import kotlin.math.*

object Geometry {
    fun circleArea(r: Double) = PI * r * r
    fun circleCirc(r: Double) = 2 * PI * r
    fun rectArea(w: Double, h: Double) = w * h
    fun triangleArea(b: Double, h: Double) = b * h / 2
    fun trapezoidArea(a: Double, b: Double, h: Double) = (a + b) / 2 * h
    fun ellipseArea(a: Double, b: Double) = PI * a * b
    fun sphereVolume(r: Double) = 4.0 / 3 * PI * r.pow(3)
    fun sphereArea(r: Double) = 4 * PI * r * r
    fun cylinderVolume(r: Double, h: Double) = PI * r * r * h
    fun coneVolume(r: Double, h: Double) = PI * r * r * h / 3
    fun cubeVolume(s: Double) = s.pow(3)
    fun prismVolume(w: Double, h: Double, d: Double) = w * h * d
    fun pyramidVolume(base: Double, h: Double) = base * base * h / 3
}

object HealthDate {
    fun bmi(weightKg: Double, heightCm: Double): Double {
        val m = heightCm / 100
        return if (m <= 0) 0.0 else weightKg / (m * m)
    }

    fun bodyFatNavy(waistCm: Double, neckCm: Double, heightCm: Double, hipCm: Double = 0.0, male: Boolean = true): Double {
        return try {
            if (male) 495 / (1.0324 - 0.19077 * log10(waistCm - neckCm) + 0.15456 * log10(heightCm)) - 450
            else 495 / (1.29579 - 0.35004 * log10(waistCm + hipCm - neckCm) + 0.22100 * log10(heightCm)) - 450
        } catch (e: Exception) { Double.NaN }
    }

    fun tdee(weightKg: Double, heightCm: Double, age: Int, male: Boolean, activity: Double = 1.55): Double {
        val bmr = if (male) 10 * weightKg + 6.25 * heightCm - 5 * age + 5
        else 10 * weightKg + 6.25 * heightCm - 5 * age - 161
        return bmr * activity
    }

    fun ohm(v: Double?, i: Double?, r: Double?): Triple<Double?, Double?, Double?> {
        var vv = v; var ii = i; var rr = r
        if (vv == null && ii != null && rr != null) vv = ii * rr
        if (ii == null && vv != null && rr != null && rr != 0.0) ii = vv / rr
        if (rr == null && vv != null && ii != null && ii != 0.0) rr = vv / ii
        return Triple(vv, ii, rr)
    }
}
