package calc.u.core

import com.ezylang.evalex.Expression
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.security.SecureRandom
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sqrt

object Engine {
    fun eval(input: String, angleDeg: Boolean = true): Result<BigDecimal> = runCatching {
        var expr = input.trim()
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
        expr = expr.replace(Regex("√\\s*\\("), "SQRT(")
        expr = expr.replace(Regex("√\\s*([0-9]+(?:\\.[0-9]+)?)"), "SQRT($1)")
        expr = expr.replace(Regex("√\\s*(PI\\b)"), "SQRT($1)")
        expr = expr.replace(Regex("√\\s*([A-Za-z_][A-Za-z0-9_.]*)"), "SQRT($1)")
        while (expr.isNotEmpty() && expr.last() in "+-*/^%×÷−") {
            expr = expr.dropLast(1).trimEnd()
        }
        if (!angleDeg) {
            expr = expr.replace(Regex("\\bSIN\\s*\\(", RegexOption.IGNORE_CASE), "SINR(")
            expr = expr.replace(Regex("\\bCOS\\s*\\(", RegexOption.IGNORE_CASE), "COSR(")
            expr = expr.replace(Regex("\\bTAN\\s*\\(", RegexOption.IGNORE_CASE), "TANR(")
        }
        val unclosed = expr.count { it == '(' } - expr.count { it == ')' }
        if (unclosed > 0) expr += ")".repeat(unclosed)
        Expression(expr).evaluate().numberValue
    }

    fun format(v: BigDecimal, maxScale: Int = 10): String {
        return try {
            val d = v.toDouble()
            if (!d.isFinite()) return "Error"
            val scaled = if (v.scale() > maxScale) v.setScale(maxScale, RoundingMode.HALF_UP) else v
            if (scaled.compareTo(BigDecimal.ZERO) == 0) return "0"
            scaled.stripTrailingZeros().toPlainString()
        } catch (e: Exception) {
            "Error"
        }
    }

    fun toFraction(value: Double, maxDenominator: Int = 1000): Pair<Long, Long>? {
        if (!value.isFinite()) return null
        if (maxDenominator < 1) return null
        if (value == 0.0) return Pair(0L, 1L)
        val sign = if (value < 0) -1L else 1L
        var x = abs(value)
        var h1 = 1L
        var h2 = 0L
        var k1 = 0L
        var k2 = 1L
        var b = x
        while (true) {
            val a = floor(b).toLong()
            val h = Math.addExact(Math.multiplyExact(a, h1), h2)
            val k = Math.addExact(Math.multiplyExact(a, k1), k2)
            if (k > maxDenominator) break
            h2 = h1
            h1 = h
            k2 = k1
            k1 = k
            if (b == a.toDouble()) break
            val rem = b - a.toDouble()
            if (rem == 0.0) break
            b = 1.0 / rem
            if (!b.isFinite()) break
        }
        if (k1 == 0L) return null
        val g = gcd(abs(h1), k1)
        return Pair(sign * h1 / g, k1 / g)
    }

    fun gcd(a: Long, b: Long): Long {
        var x = a
        var y = b
        while (y != 0L) {
            val t = x % y
            x = y
            y = t
        }
        return abs(x)
    }

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        val g = gcd(a, b)
        return abs(Math.multiplyExact(a / g, b))
    }

    fun isPrime(n: Long): Boolean {
        if (n < 2L) return false
        if (n == 2L || n == 3L) return true
        if (n % 2L == 0L || n % 3L == 0L) return false
        var i = 5L
        while (i <= n / i) {
            if (n % i == 0L || n % (i + 2L) == 0L) return false
            i += 6L
        }
        return true
    }

    fun nCr(n: Long, r: Long): Long {
        if (r < 0L || r > n) return 0L
        val rr = if (r < n - r) r else n - r
        var res = BigInteger.ONE
        for (i in 1L..rr) {
            res = res.multiply(BigInteger.valueOf(n - rr + i)).divide(BigInteger.valueOf(i))
        }
        return res.longValueExact()
    }

    fun nPr(n: Long, r: Long): Long {
        if (r < 0L || r > n) return 0L
        var res = BigInteger.ONE
        for (i in 0L until r) {
            res = res.multiply(BigInteger.valueOf(n - i))
        }
        return res.longValueExact()
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): List<String> {
        if (a == 0.0) {
            if (b == 0.0) return emptyList()
            return listOf(fmt(-c / b))
        }
        val d = b * b - 4 * a * c
        if (d < 0) {
            val real = -b / (2 * a)
            val imag = sqrt(-d) / (2 * abs(a))
            return listOf("${fmt(real)}±${fmt(imag)}i")
        }
        if (d == 0.0) return listOf(fmt(-b / (2 * a)))
        val s = sqrt(d)
        return listOf(fmt((-b - s) / (2 * a)), fmt((-b + s) / (2 * a)))
    }

    fun solveLinearSystem2x2(a1: Double, b1: Double, c1: Double, a2: Double, b2: Double, c2: Double): Pair<String, String> {
        val det = a1 * b2 - a2 * b1
        if (det == 0.0) return Pair("—", "—")
        return Pair(fmt((c1 * b2 - c2 * b1) / det), fmt((a1 * c2 - a2 * c1) / det))
    }

    fun factorial(n: Long): Long {
        require(n >= 0L) { "n must be >= 0" }
        var res = 1L
        for (i in 2L..n) {
            res = Math.multiplyExact(res, i)
        }
        return res
    }

    fun mean(values: List<Double>, type: String = "arithmetic"): Double {
        require(values.isNotEmpty()) { "values must not be empty" }
        return when (type.lowercase()) {
            "arithmetic", "a" -> values.sum() / values.size
            "geometric", "g" -> {
                require(values.all { it > 0.0 }) { "geometric mean requires positive values" }
                exp(values.sumOf { ln(it) } / values.size)
            }
            "harmonic", "h" -> {
                require(values.all { it != 0.0 }) { "harmonic mean requires non-zero values" }
                values.size / values.sumOf { 1.0 / it }
            }
            else -> throw IllegalArgumentException("unknown mean type: $type")
        }
    }

    private fun fmt(d: Double): String {
        if (!d.isFinite()) return "Error"
        if (d == 0.0) return "0"
        val s = BigDecimal.valueOf(d).stripTrailingZeros().toPlainString()
        return if (s == "-0") "0" else s
    }

    fun bitwiseAnd(a: Long, b: Long): Long = a and b
    fun bitwiseOr(a: Long, b: Long): Long = a or b
    fun bitwiseXor(a: Long, b: Long): Long = a xor b
    fun bitwiseNot(a: Long): Long = a.inv()
    fun shl(a: Long, bits: Int): Long = a shl bits
    fun shr(a: Long, bits: Int): Long = a shr bits

    fun randomInt(min: Int, max: Int, rng: SecureRandom = SecureRandom()): Int {
        val lo = minOf(min, max)
        val hi = maxOf(min, max)
        if (lo == hi) return lo
        val bound = hi.toLong() - lo + 1L
        if (bound <= Int.MAX_VALUE) return lo + rng.nextInt(bound.toInt())
        var r: Long
        do {
            r = rng.nextLong() ushr 1
        } while (r >= Long.MAX_VALUE - Long.MAX_VALUE % bound)
        return (lo + r % bound).toInt()
    }

    fun randomDecimal(): Double = SecureRandom().nextDouble()

    fun solveCubic(a: Double, b: Double, c: Double, d: Double): List<String> {
        if (a == 0.0) return solveQuadratic(b, c, d)
        val B = b / a
        val C = c / a
        val D = d / a
        val p = C - B * B / 3.0
        val q = 2.0 * B * B * B / 27.0 - B * C / 3.0 + D
        val disc = (q / 2.0) * (q / 2.0) + (p / 3.0) * (p / 3.0) * (p / 3.0)
        val shift = B / 3.0
        val eps = 1e-12
        if (disc > eps) {
            val s = sqrt(disc)
            val u = cbrt(-q / 2.0 + s)
            val v = cbrt(-q / 2.0 - s)
            val x1 = u + v - shift
            val real = -(u + v) / 2.0 - shift
            val imag = abs(u - v) * sqrt(3.0) / 2.0
            return listOf(fmt(x1), "${fmt(real)}±${fmt(imag)}i")
        }
        if (abs(disc) <= eps) {
            val u = cbrt(-q / 2.0)
            val x1 = 2 * u - shift
            val x2 = -u - shift
            if (abs(x1 - x2) < 1e-9) return listOf(fmt(x1))
            return listOf(fmt(x1), fmt(x2))
        }
        val r = 2.0 * sqrt(-p / 3.0)
        val arg = (-q / 2.0) / sqrt(-(p / 3.0) * (p / 3.0) * (p / 3.0))
        val phi = acos(arg.coerceIn(-1.0, 1.0))
        return (0..2).map { k ->
            fmt(r * cos((phi - 2.0 * PI * k) / 3.0) - shift)
        }.sortedBy { it.toDoubleOrNull() ?: Double.NaN }
    }

    fun statsMedian(values: List<Double>): Double {
        require(values.isNotEmpty()) { "values must not be empty" }
        val s = values.sorted()
        return if (s.size % 2 == 1) s[s.size / 2]
        else (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0
    }

    fun statsMode(values: List<Double>): Double {
        require(values.isNotEmpty()) { "values must not be empty" }
        return values.groupingBy { it }.eachCount()
            .entries.sortedWith(compareByDescending<Map.Entry<Double, Int>> { it.value }.thenBy { it.key })
            .first().key
    }

    fun statsVariance(values: List<Double>): Double {
        require(values.isNotEmpty()) { "values must not be empty" }
        val m = values.sum() / values.size
        return values.sumOf { (it - m) * (it - m) } / values.size
    }

    fun statsStdev(values: List<Double>): Double = sqrt(statsVariance(values))

    private fun evalAt(expr: String, xVal: Double, angleDeg: Boolean): Double? {
        val sub = expr.replace(Regex("\\bx\\b"), "($xVal)")
        return eval(sub, angleDeg).getOrNull()?.toDouble()
    }

    fun derivative(expr: String, x: Double, angleDeg: Boolean = true): Double {
        val h = 1e-5
        val f1 = evalAt(expr, x + h, angleDeg) ?: return Double.NaN
        val f2 = evalAt(expr, x - h, angleDeg) ?: return Double.NaN
        return (f1 - f2) / (2 * h)
    }

    fun integral(expr: String, a: Double, b: Double, angleDeg: Boolean = true): Double {
        val n = 1000
        val h = (b - a) / n
        var sum = (evalAt(expr, a, angleDeg) ?: return Double.NaN) +
            (evalAt(expr, b, angleDeg) ?: return Double.NaN)
        for (i in 1 until n) {
            val fx = evalAt(expr, a + i * h, angleDeg) ?: return Double.NaN
            sum += if (i % 2 == 1) 4 * fx else 2 * fx
        }
        return sum * h / 3.0
    }
}
