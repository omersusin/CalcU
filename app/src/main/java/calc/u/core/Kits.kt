package calc.u.core

import kotlin.math.PI
import kotlin.math.acos
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
    fun solveTriangleSSS(a: Double, b: Double, c: Double): Map<String, Double> {
        require(a > 0 && b > 0 && c > 0) { "sides must be > 0" }
        require(a + b > c && a + c > b && b + c > a) { "triangle inequality violated" }
        fun angle(opposite: Double, s1: Double, s2: Double): Double {
            val cosv = ((s1 * s1 + s2 * s2 - opposite * opposite) / (2 * s1 * s2)).coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(cosv))
        }
        val angleA = angle(a, b, c)
        val angleB = angle(b, a, c)
        val angleC = 180.0 - angleA - angleB
        val perimeter = a + b + c
        val s = perimeter / 2
        val area = sqrt(s * (s - a) * (s - b) * (s - c))
        return mapOf("angleA" to angleA, "angleB" to angleB, "angleC" to angleC, "perimeter" to perimeter, "area" to area)
    }
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

object ColorKit {
    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        val h = hex.trim().removePrefix("#")
        require(h.length == 6) { "hex must be 6 digits" }
        require(h.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) { "hex contains invalid digits" }
        return Triple(h.substring(0, 2).toInt(16), h.substring(2, 4).toInt(16), h.substring(4, 6).toInt(16))
    }
    fun rgbToHex(r: Int, g: Int, b: Int): String {
        require(r in 0..255) { "r must be in 0..255" }
        require(g in 0..255) { "g must be in 0..255" }
        require(b in 0..255) { "b must be in 0..255" }
        return "#%02X%02X%02X".format(r, g, b)
    }
    fun rgbToHsl(r: Int, g: Int, b: Int): Triple<Double, Double, Double> {
        require(r in 0..255) { "r must be in 0..255" }
        require(g in 0..255) { "g must be in 0..255" }
        require(b in 0..255) { "b must be in 0..255" }
        val rf = r / 255.0
        val gf = g / 255.0
        val bf = b / 255.0
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val l = (max + min) / 2
        if (max == min) return Triple(0.0, 0.0, l)
        val d = max - min
        val s = if (l > 0.5) d / (2 - max - min) else d / (max + min)
        val h = (when (max) {
            rf -> (gf - bf) / d + (if (gf < bf) 6 else 0)
            gf -> (bf - rf) / d + 2
            else -> (rf - gf) / d + 4
        }) * 60
        return Triple(h, s, l)
    }
    fun hslToRgb(h: Double, s: Double, l: Double): Triple<Int, Int, Int> {
        require(h in 0.0..360.0) { "h must be in 0..360" }
        require(s in 0.0..1.0) { "s must be in 0..1" }
        require(l in 0.0..1.0) { "l must be in 0..1" }
        if (s == 0.0) {
            val v = (l * 255 + 0.5).toInt().coerceIn(0, 255)
            return Triple(v, v, v)
        }
        fun hue2rgb(p: Double, q: Double, t: Double): Double {
            var tt = t
            if (tt < 0) tt += 1
            if (tt > 1) tt -= 1
            return when {
                tt < 1.0 / 6 -> p + (q - p) * 6 * tt
                tt < 1.0 / 2 -> q
                tt < 2.0 / 3 -> p + (q - p) * (2.0 / 3 - tt) * 6
                else -> p
            }
        }
        val q = if (l < 0.5) l * (1 + s) else l + s - l * s
        val p = 2 * l - q
        val hk = h / 360.0
        return Triple(
            (hue2rgb(p, q, hk + 1.0 / 3) * 255 + 0.5).toInt().coerceIn(0, 255),
            (hue2rgb(p, q, hk) * 255 + 0.5).toInt().coerceIn(0, 255),
            (hue2rgb(p, q, hk - 1.0 / 3) * 255 + 0.5).toInt().coerceIn(0, 255)
        )
    }
}

object ScreenKit {
    private fun gcd(a: Int, b: Int): Int {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y != 0) {
            val t = x % y
            x = y
            y = t
        }
        return x
    }
    fun aspectRatio(w: Int, h: Int): String {
        require(w > 0) { "w must be > 0" }
        require(h > 0) { "h must be > 0" }
        val g = gcd(w, h)
        return "${w / g}:${h / g}"
    }
    fun ppi(wPx: Int, hPx: Int, diagonalIn: Double): Double {
        require(wPx > 0) { "wPx must be > 0" }
        require(hPx > 0) { "hPx must be > 0" }
        require(diagonalIn > 0) { "diagonalIn must be > 0" }
        return sqrt(wPx.toDouble() * wPx + hPx.toDouble() * hPx) / diagonalIn
    }
}

object TripKit {
    fun fuelCost(distanceKm: Double, per100km: Double, pricePerL: Double): Double {
        require(distanceKm >= 0) { "distanceKm must be >= 0" }
        require(per100km >= 0) { "per100km must be >= 0" }
        require(pricePerL >= 0) { "pricePerL must be >= 0" }
        return distanceKm / 100 * per100km * pricePerL
    }
    fun tripTime(distanceKm: Double, avgKmh: Double): Double {
        require(distanceKm >= 0) { "distanceKm must be >= 0" }
        require(avgKmh > 0) { "avgKmh must be > 0" }
        return distanceKm / avgKmh
    }
}

object ClockKit {
    fun weekdayName(year: Int, month: Int, day: Int): String {
        val d = java.time.LocalDate.of(year, month, day)
        return d.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
    }
    fun daysUntil(year: Int, month: Int, day: Int): Long {
        val today = java.time.LocalDate.now()
        val target = java.time.LocalDate.of(year, month, day)
        return java.time.temporal.ChronoUnit.DAYS.between(today, target)
    }
    fun worldTime(zoneId: String): String {
        val zone = try {
            java.time.ZoneId.of(zoneId.trim())
        } catch (e: Exception) {
            return "unknown zone"
        }
        return java.time.ZonedDateTime.now(zone).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    }
}

object VectorKit {
    fun dot(a: List<Double>, b: List<Double>): Double {
        require(a.size == b.size) { "vectors must have same size" }
        return a.indices.sumOf { a[it] * b[it] }
    }
    fun cross(a: List<Double>, b: List<Double>): List<Double> {
        require(a.size == 3 && b.size == 3) { "cross requires 3D vectors" }
        return listOf(
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        )
    }
    fun magnitude(a: List<Double>): Double {
        require(a.isNotEmpty()) { "vector must not be empty" }
        return sqrt(a.sumOf { it * it })
    }
    fun angleDeg(a: List<Double>, b: List<Double>): Double {
        val ma = magnitude(a)
        val mb = magnitude(b)
        require(ma > 0 && mb > 0) { "vectors must be non-zero" }
        val cosv = (dot(a, b) / (ma * mb)).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cosv))
    }
}

object ClockAngle {
    fun angle(hour: Int, min: Int): Double {
        require(hour in 0..23) { "hour must be in 0..23" }
        require(min in 0..59) { "min must be in 0..59" }
        val hourA = (hour % 12) * 30.0 + min * 0.5
        val minA = min * 6.0
        val d = kotlin.math.abs(hourA - minA) % 360.0
        return if (d > 180.0) 360.0 - d else d
    }
}

object PaintKit {
    fun paintLiters(areaM2: Double, coats: Int, coveragePerLiter: Double): Double {
        require(areaM2 >= 0) { "areaM2 must be >= 0" }
        require(coats > 0) { "coats must be > 0" }
        require(coveragePerLiter > 0) { "coveragePerLiter must be > 0" }
        return areaM2 * coats / coveragePerLiter
    }
    fun tilesNeeded(areaM2: Double, tileLenCm: Double, tileWidCm: Double, wastePct: Double): Long {
        require(areaM2 >= 0) { "areaM2 must be >= 0" }
        require(tileLenCm > 0) { "tileLenCm must be > 0" }
        require(tileWidCm > 0) { "tileWidCm must be > 0" }
        require(wastePct >= 0) { "wastePct must be >= 0" }
        val tileM2 = (tileLenCm / 100.0) * (tileWidCm / 100.0)
        val raw = areaM2 / tileM2 * (1.0 + wastePct / 100.0)
        return kotlin.math.ceil(raw).toLong()
    }
}

object IdealWeight {
    fun devine(heightCm: Double, male: Boolean): Double {
        require(heightCm > 0) { "heightCm must be > 0" }
        return if (male) 50.0 + 0.91 * (heightCm - 152.4)
        else 45.5 + 0.91 * (heightCm - 152.4)
    }
    fun robinson(heightCm: Double, male: Boolean): Double {
        require(heightCm > 0) { "heightCm must be > 0" }
        val inches = heightCm / 2.54
        return if (male) 52.0 + 1.9 * (inches - 60.0)
        else 49.0 + 1.7 * (inches - 60.0)
    }
}
