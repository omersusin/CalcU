package calc.u.core

import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

object Geometry {
    fun circleArea(r: Double) = PI * r * r
    fun circleCirc(r: Double) = 2 * PI * r
    fun rectArea(w: Double, h: Double) = w * h
    fun triangleArea(b: Double, h: Double) = b * h / 2
    fun trapezoidArea(a: Double, b: Double, h: Double) = (a + b) / 2 * h
    fun ellipseArea(a: Double, b: Double) = PI * a * b
    fun sphereVolume(r: Double) = 4.0 / 3 * PI * r.pow(3)
    fun sphereArea(r: Double) = 4 * PI * r * r
    fun sphereSurface(r: Double) = 4 * PI * r * r
    fun cylinderVolume(r: Double, h: Double) = PI * r * r * h
    fun coneVolume(r: Double, h: Double) = PI * r * r * h / 3
    fun coneSurface(r: Double, h: Double) = PI * r * (r + sqrt(h * h + r * r))
    fun cubeVolume(s: Double) = s.pow(3)
    fun prismVolume(w: Double, h: Double, d: Double) = w * h * d
    fun rectPrismVolume(w: Double, h: Double, d: Double) = w * h * d
    fun rectPrismSurface(w: Double, h: Double, d: Double) = 2 * (w * h + h * d + w * d)
    fun pyramidVolume(base: Double, h: Double) = base * base * h / 3
}

object HealthDate {
    fun bmi(weightKg: Double, heightCm: Double): Double {
        require(weightKg > 0) { "weightKg must be > 0" }
        require(heightCm > 0) { "heightCm must be > 0" }
        val m = heightCm / 100
        return weightKg / (m * m)
    }

    fun bodyFatNavy(waistCm: Double, neckCm: Double, heightCm: Double, hipCm: Double = 0.0, male: Boolean = true): Double {
        require(waistCm > 0) { "waistCm must be > 0" }
        require(neckCm > 0) { "neckCm must be > 0" }
        require(heightCm > 0) { "heightCm must be > 0" }
        return if (male) {
            require(waistCm > neckCm) { "waistCm must exceed neckCm for males" }
            495 / (1.0324 - 0.19077 * log10(waistCm - neckCm) + 0.15456 * log10(heightCm)) - 450
        } else {
            require(hipCm > 0) { "hipCm must be > 0 for females" }
            require(waistCm + hipCm > neckCm) { "waistCm + hipCm must exceed neckCm" }
            495 / (1.29579 - 0.35004 * log10(waistCm + hipCm - neckCm) + 0.22100 * log10(heightCm)) - 450
        }
    }

    fun tdee(weightKg: Double, heightCm: Double, age: Int, male: Boolean, activity: Double = 1.55): Double {
        require(weightKg > 0) { "weightKg must be > 0" }
        require(heightCm > 0) { "heightCm must be > 0" }
        require(age in 0..150) { "age must be in 0..150" }
        require(activity > 0) { "activity must be > 0" }
        val bmr = if (male) 10 * weightKg + 6.25 * heightCm - 5 * age + 5
        else 10 * weightKg + 6.25 * heightCm - 5 * age - 161
        return bmr * activity
    }

    fun ohm(v: Double?, i: Double?, r: Double?): Triple<Double?, Double?, Double?> {
        var vv = v
        var ii = i
        var rr = r
        if (vv == null && ii != null && rr != null) vv = ii * rr
        if (ii == null && vv != null && rr != null && rr != 0.0) ii = vv / rr
        if (rr == null && vv != null && ii != null && ii != 0.0) rr = vv / ii
        return Triple(vv, ii, rr)
    }

    fun ageYears(birthEpochDay: Long, nowEpochDay: Long): Triple<Int, Int, Int> {
        require(nowEpochDay >= birthEpochDay) { "nowEpochDay must be >= birthEpochDay" }
        val (by, bm, bd) = dayToCivil(birthEpochDay)
        val (ny, nm, nd) = dayToCivil(nowEpochDay)
        var years = ny - by
        var months = nm - bm
        var days = nd - bd
        if (days < 0) {
            months -= 1
            val pm = if (nm - 1 < 1) 12 else nm - 1
            val py = if (nm - 1 < 1) ny - 1 else ny
            days += daysInMonth(py, pm)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }
        return Triple(years, months, days)
    }

    private fun dayToCivil(z: Long): Triple<Int, Int, Int> {
        var zz = z + 719468
        val era = if (zz >= 0) zz / 146097 else (zz - 146096) / 146097
        val doe = (zz - era * 146097).toInt()
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        var y = (yoe + era * 400).toInt()
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = if (mp < 10) mp + 3 else mp - 9
        if (m <= 2) y += 1
        return Triple(y, m, d)
    }

    private fun daysInMonth(y: Int, m: Int): Int = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> if ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) 29 else 28
    }
}
