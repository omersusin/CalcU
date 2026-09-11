package calc.u.core

import com.ezylang.evalex.Expression
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
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
        if (!angleDeg) {
            expr = expr.replace(Regex("\\bSIN\\s*\\(", RegexOption.IGNORE_CASE), "SINR(")
            expr = expr.replace(Regex("\\bCOS\\s*\\(", RegexOption.IGNORE_CASE), "COSR(")
            expr = expr.replace(Regex("\\bTAN\\s*\\(", RegexOption.IGNORE_CASE), "TANR(")
        }
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
}
