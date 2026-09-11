package calc.u.core

import com.ezylang.evalex.Expression
import java.math.BigDecimal
import kotlin.math.*

object Engine {
    fun eval(input: String, angleDeg: Boolean = true): Result<BigDecimal> = runCatching {
        var expr = input.trim()
            .replace("×", "*").replace("÷", "/").replace("−", "-")
            .replace("π", "PI")
            .replace("√", "SQRT")
        if (!angleDeg) {
            expr = expr.replace("SIN(", "SINR(", true)
                .replace("COS(", "COSR(", true)
                .replace("TAN(", "TANR(", true)
        }
        Expression(expr).evaluate().numberValue
    }

    fun toFraction(value: Double, maxDenominator: Int = 1000): Pair<Long, Long>? {
        if (!value.isFinite()) return null
        val sign = if (value < 0) -1 else 1
        var x = abs(value)
        var h1 = 1L; var h2 = 0L; var k1 = 0L; var k2 = 1L
        var b = x
        do {
            val a = floor(b).toLong()
            val h = a * h1 + h2; val k = a * k1 + k2
            if (k > maxDenominator) break
            h2 = h1; h1 = h; k2 = k1; k1 = k
            if (b == a.toDouble()) break
            b = 1.0 / (b - a)
        } while (true)
        val g = gcd(abs(h1), k1)
        return Pair(sign * h1 / g, k1 / g)
    }

    fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)
    fun lcm(a: Long, b: Long): Long = abs(a / gcd(a, b) * b)

    fun isPrime(n: Long): Boolean {
        if (n < 2) return false
        if (n % 2 == 0L) return n == 2L
        var i = 3L
        while (i * i <= n) { if (n % i == 0L) return false; i += 2 }
        return true
    }

    fun nCr(n: Long, r: Long): Long {
        if (r < 0 || r > n) return 0
        var rr = minOf(r, n - r); var res = 1L
        for (i in 1..rr) res = res * (n - rr + i) / i
        return res
    }

    fun nPr(n: Long, r: Long): Long {
        if (r < 0 || r > n) return 0
        var res = 1L
        for (i in 0 until r) res *= (n - i)
        return res
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): List<Double> {
        if (a == 0.0) return if (b == 0.0) emptyList() else listOf(-c / b)
        val d = b * b - 4 * a * c
        if (d < 0) return emptyList()
        if (d == 0.0) return listOf(-b / (2 * a))
        val s = sqrt(d)
        return listOf((-b - s) / (2 * a), (-b + s) / (2 * a))
    }

    fun format(v: BigDecimal, maxScale: Int = 10): String {
        val s = v.stripTrailingZeros().toPlainString()
        return s
    }
}
