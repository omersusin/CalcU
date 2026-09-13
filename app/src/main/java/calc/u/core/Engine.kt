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
    /** A user-defined variable assignment, e.g. `radius = 12.5`. */
    data class Assignment(val name: String, val value: BigDecimal)

    /**
     * Names never treated as user variables — math/functions constants that
     * substitution must not clobber (EvalEx builtins + our rewrite targets).
     */
    private val RESERVED_VARS: Set<String> = setOf(
        "PI", "E", "LN", "LOG", "LOG10", "LN2", "LN10", "SQRT", "CBRT",
        "ABS", "SIGN", "ROUND", "FLOOR", "CEIL", "MIN", "MAX", "POW", "MOD", "EXP",
        "SIN", "COS", "TAN", "SINR", "COSR", "TANR", "ASIN", "ACOS", "ATAN",
        "FACT", "GCD", "LCM", "NCR", "NPR", "TOTIENT", "FIB", "ISPRIME"
    )

    /** Parse `name = rhs` into an assignment, or `null` when not assignable. */
    fun parseAssignment(input: String, angleMode: String = "DEG"): Assignment? = runCatching {
        val t = input.trim()
        val m = Regex("^([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+)$").find(t) ?: return null
        val name = m.groupValues[1]
        if (name.uppercase() in RESERVED_VARS) return null
        val value = evalMode(m.groupValues[2], angleMode).getOrElse { return null }
        Assignment(name, value)
    }.getOrNull()

    private fun isWordChar(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '.'

    /** Substitute defined variable names in [input]; reserved words are skipped. */
    fun substitute(input: String, vars: Map<String, BigDecimal>): String {
        if (vars.isEmpty()) return input
        val out = StringBuilder(input.length + 16)
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (!(c.isLetter() || c == '_') || (i > 0 && isWordChar(input[i - 1]))) {
                out.append(c)
                i++
                continue
            }
            var j = i
            while (j < input.length && (input[j].isLetterOrDigit() || input[j] == '_')) j++
            val word = input.substring(i, j)
            if (word.uppercase() in RESERVED_VARS || word !in vars) {
                out.append(word)
            } else {
                out.append(vars.getValue(word).toPlainString())
            }
            i = j
        }
        return out.toString()
    }

    fun eval(input: String, angleDeg: Boolean = true, vars: Map<String, BigDecimal> = emptyMap()): Result<BigDecimal> = runCatching {
        require(input.length <= 20000) { "expression too long" }
        var expr = substitute(input.trim(), vars)
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
        expr = expr.replace(Regex("√\\s*\\("), "SQRT(")
        expr = expr.replace(Regex("√\\s*([0-9]+(?:\\.[0-9]+)?)"), "SQRT($1)")
        expr = expr.replace(Regex("√\\s*(PI\\b)"), "SQRT($1)")
        expr = expr.replace(Regex("√\\s*([A-Za-z_][A-Za-z0-9_.]*)"), "SQRT($1)")
        expr = expr.replace(Regex("\\bln\\s*\\(", RegexOption.IGNORE_CASE), "LOG(")
        expr = expr.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*!\\s*(?![A-Za-z_0-9])"), "FACT($1)")
        expr = expr.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*%"), "$1/100")
        while (expr.isNotEmpty() && expr.last() in "+-*/^%×÷−") {
            expr = expr.dropLast(1).trimEnd()
        }
        if (!angleDeg) {
            expr = expr.replace(Regex("\\bSIN\\s*\\(", RegexOption.IGNORE_CASE), "SINR(")
            expr = expr.replace(Regex("\\bCOS\\s*\\(", RegexOption.IGNORE_CASE), "COSR(")
            expr = expr.replace(Regex("\\bTAN\\s*\\(", RegexOption.IGNORE_CASE), "TANR(")
        }
        val unclosed = expr.count { it == '(' } - expr.count { it == ')' }
        require(unclosed <= 1000) { "too many unclosed parentheses" }
        if (unclosed > 0) expr += ")".repeat(unclosed)
        Expression(expr).evaluate().numberValue
    }

    /**
     * String angle-mode entry point. [mode] is one of `"DEG"`, `"RAD"` or `"GRA"`
     * (case-insensitive, trimmed; anything else falls back to `"DEG"`).
     *
     * - `DEG` reuses [eval] with `angleDeg = true` (EvalEx degree trig).
     * - `RAD` reuses [eval] with `angleDeg = false` (`SIN(`→`SINR(` path).
     * - `GRA` (gradians, 400 per circle) reuses the `DEG` engine after rewriting
     *   bare `SIN(`/`COS(`/`TAN(` arguments with a 0.9 grad→deg factor, e.g.
     *   `SIN(x)` → `SIN(0.9*(x))`, via balanced-paren matching.
     *
     * Simple-case limitation: only forward bare `SIN`/`COS`/`TAN` with explicit
     * parentheses are rewritten. Inverse trig (`ASIN(`/`ACOS(`/`ATAN(`) still
     * returns degrees, and explicit radian spellings (`SINR(`/`COSR(`/`TANR(`)
     * are left untouched.
     */
    fun evalMode(input: String, mode: String = "DEG", vars: Map<String, BigDecimal> = emptyMap()): Result<BigDecimal> = runCatching {
        when (mode.trim().uppercase()) {
            "RAD" -> eval(input, false, vars).getOrThrow()
            "GRA" -> eval(gradToDeg(input), true, vars).getOrThrow()
            else -> eval(input, true, vars).getOrThrow()
        }
    }

    private fun gradToDeg(expr: String): String {
        val out = StringBuilder(expr.length + 16)
        var i = 0
        while (i < expr.length) {
            val hit = matchBareTrig(expr, i)
            if (hit == null) {
                out.append(expr[i])
                i++
                continue
            }
            val (name, openIdx) = hit
            var depth = 0
            var closeIdx = -1
            var j = openIdx
            while (j < expr.length) {
                when (expr[j]) {
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0) {
                            closeIdx = j
                            break
                        }
                    }
                }
                j++
            }
            if (closeIdx == -1) {
                out.append(name).append("(0.9*(")
                out.append(gradToDeg(expr.substring(openIdx + 1)))
                i = expr.length
            } else {
                out.append(name).append("(0.9*(")
                out.append(gradToDeg(expr.substring(openIdx + 1, closeIdx)))
                out.append("))")
                i = closeIdx + 1
            }
        }
        return out.toString()
    }

    private fun matchBareTrig(expr: String, i: Int): Pair<String, Int>? {
        if (i + 3 > expr.length) return null
        val w = expr.substring(i, i + 3)
        if (!w.equals("SIN", ignoreCase = true) &&
            !w.equals("COS", ignoreCase = true) &&
            !w.equals("TAN", ignoreCase = true)
        ) return null
        if (i > 0 && (expr[i - 1].isLetterOrDigit() || expr[i - 1] == '_')) return null
        var k = i + 3
        while (k < expr.length && expr[k].isWhitespace()) k++
        if (k >= expr.length || expr[k] != '(') return null
        return Pair(w, k)
    }

    fun format(v: BigDecimal, maxScale: Int = 10, grouping: String = "locale", fractions: Boolean = false): String {
        return try {
            val d = v.toDouble()
            if (!d.isFinite()) return "Error"
            val scaled = if (v.scale() > maxScale) v.setScale(maxScale, RoundingMode.HALF_UP) else v
            if (scaled.compareTo(BigDecimal.ZERO) == 0) return "0"
            val stripped = scaled.stripTrailingZeros()
            if (stripped.compareTo(BigDecimal.ZERO) == 0) return "0"
            if (fractions && stripped.scale() > 6) {
                toFraction(d)?.let { (n, den) -> return "$n/$den" }
            }
            val symbols = java.text.DecimalFormatSymbols.getInstance()
            val df = java.text.DecimalFormat().apply {
                decimalFormatSymbols = symbols
                isGroupingUsed = grouping != "none"
                groupingSize = 3
                maximumFractionDigits = maxScale.coerceAtLeast(0)
                minimumFractionDigits = 0
                roundingMode = RoundingMode.HALF_UP
                isDecimalSeparatorAlwaysShown = false
            }
            val plain = df.format(stripped)
            if (stripped.precision() > 14 && d.isFinite()) {
                return scientific(stripped, symbols, maxScale.coerceIn(0, 12))
            }
            when (grouping) {
                "comma" -> plain
                    .replace(symbols.groupingSeparator.toString(), "‚")
                    .replace(symbols.decimalSeparator.toString(), ".")
                    .replace("‚", ",")
                "space" -> plain
                    .replace(symbols.groupingSeparator.toString(), "‚")
                    .replace(symbols.decimalSeparator.toString(), ".")
                    .replace("‚", " ")
                "indian" -> indianFormat(stripped, symbols.decimalSeparator)
                else -> plain
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun scientific(v: BigDecimal, symbols: java.text.DecimalFormatSymbols, maxScale: Int): String {
        val pattern = buildString {
            append("0")
            if (maxScale > 0) {
                append('.')
                repeat(maxScale) { append('#') }
            }
            append("E0")
        }
        val df = java.text.DecimalFormat(pattern).apply {
            decimalFormatSymbols = symbols
            isGroupingUsed = false
            roundingMode = RoundingMode.HALF_UP
        }
        return df.format(v).replace("E", "E+").replace("E+-", "E-")
    }

    private fun indianFormat(v: BigDecimal, decimalSep: Char): String {
        val s = v.toPlainString()
        val dot = s.indexOf('.')
        val intPart = if (dot < 0) s else s.substring(0, dot)
        val fracPart = if (dot < 0) "" else s.substring(dot)
        val neg = intPart.startsWith("-")
        val digits = if (neg) intPart.drop(1) else intPart
        if (digits.length <= 3) return s
        val tail = digits.takeLast(3)
        var head = digits.dropLast(3)
        val groups = ArrayDeque<String>()
        while (head.length > 2) {
            groups.addFirst(head.takeLast(2))
            head = head.dropLast(2)
        }
        if (head.isNotEmpty()) groups.addFirst(head)
        return (if (neg) "-" else "") + groups.joinToString(",") + "," + tail + fracPart.replace('.', decimalSep)
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
            val h = try {
                Math.addExact(Math.multiplyExact(a, h1), h2)
            } catch (e: ArithmeticException) {
                return null
            }
            val k = try {
                Math.addExact(Math.multiplyExact(a, k1), k2)
            } catch (e: ArithmeticException) {
                return null
            }
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
            if (Thread.currentThread().isInterrupted) {
                throw java.util.concurrent.CancellationException("isPrime cancelled")
            }
            if (n % i == 0L || n % (i + 2L) == 0L) return false
            i += 6L
        }
        return true
    }

    fun nCr(n: Long, r: Long): Long {
        if (r < 0L || r > n) return 0L
        if (n < 0L) return 0L
        val rr = if (r < n - r) r else n - r
        require(rr <= 10000L) { "nCr too large" }
        require(n <= 100000L) { "nCr too large" }
        var res = BigInteger.ONE
        for (i in 1L..rr) {
            res = res.multiply(BigInteger.valueOf(n - rr + i)).divide(BigInteger.valueOf(i))
        }
        return exactLong(res)
    }

    fun nPr(n: Long, r: Long): Long {
        if (r < 0L || r > n) return 0L
        if (n < 0L) return 0L
        require(r <= 10000L) { "nPr too large" }
        require(n <= 100000L) { "nPr too large" }
        var res = BigInteger.ONE
        for (i in 0L until r) {
            res = res.multiply(BigInteger.valueOf(n - i))
        }
        return exactLong(res)
    }

    private fun exactLong(v: BigInteger): Long {
        if (v < BigInteger.valueOf(Long.MIN_VALUE) || v > BigInteger.valueOf(Long.MAX_VALUE)) {
            throw ArithmeticException("Long overflow")
        }
        return v.toLong()
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

    /**
     * Solves a 2x2 linear system. A determinant within 1e-12 scaled by
     * coefficient magnitude counts as singular and yields ("—", "—").
     * The 1e-12 constant is shared with [solve3x3] (absolute there);
     * Matrix.inverse() instead requires exact non-zero det, since silently
     * inverting a near-singular matrix is worse than reporting it.
     */
    fun solveLinearSystem2x2(a1: Double, b1: Double, c1: Double, a2: Double, b2: Double, c2: Double): Pair<String, String> {
        val det = a1 * b2 - a2 * b1
        val scale = abs(a1) * abs(b2) + abs(a2) * abs(b1)
        if (abs(det) <= 1e-12 * maxOf(1.0, scale)) return Pair("—", "—")
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
            .firstOrNull()?.key ?: throw IllegalArgumentException("values must not be empty")
    }

    fun statsVariance(values: List<Double>): Double {
        require(values.isNotEmpty()) { "values must not be empty" }
        val m = values.sum() / values.size
        return values.sumOf { (it - m) * (it - m) } / values.size
    }

    fun statsStdev(values: List<Double>): Double = sqrt(statsVariance(values))

    private fun evalAt(expr: String, xVal: Double, angleDeg: Boolean): Double? {
        val sub = expr.replace(Regex("\\bx\\b", RegexOption.IGNORE_CASE), "($xVal)")
        return eval(sub, angleDeg).getOrNull()?.toDouble()
    }

    fun derivative(expr: String, x: Double, angleDeg: Boolean = true): Double {
        val h = 1e-5 * maxOf(1.0, abs(x))
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

    /**
     * Solves a 3x3 system via Cramer's rule on Matrix determinants.
     * Singularity threshold is an absolute |det| <= 1e-12, sharing the 1e-12
     * constant with [solveLinearSystem2x2] (magnitude-scaled there).
     * Matrix.inverse() instead requires exact non-zero det; the solvers are
     * deliberately more forgiving since they only report "no unique solution".
     */
    fun solve3x3(a: List<List<Double>>, b: List<Double>): List<String> {
        require(a.size == 3 && a.all { it.size == 3 }) { "a must be 3x3" }
        require(b.size == 3) { "b must have 3 entries" }
        require(a.all { row -> row.all { it.isFinite() } } && b.all { it.isFinite() }) { "entries must be finite" }
        fun detOf(grid: List<List<Double>>): Double {
            val flat = DoubleArray(9) { grid[it / 3][it % 3] }
            return Matrix(3, 3, flat).determinant()
        }
        val det = detOf(a)
        if (abs(det) <= 1e-12) return listOf("no unique solution")
        return (0..2).map { col ->
            val replaced = List(3) { r -> List(3) { c -> if (c == col) b[r] else a[r][c] } }
            format(BigDecimal.valueOf(detOf(replaced) / det))
        }
    }

    fun diceRoll(sides: Int, rng: SecureRandom = SecureRandom()): Int {
        require(sides >= 2) { "sides must be >= 2" }
        return rng.nextInt(sides) + 1
    }

    fun coinFlip(rng: SecureRandom = SecureRandom()): String {
        return if (rng.nextBoolean()) "Heads" else "Tails"
    }

    fun numberToWords(n: Long): String {
        require(n in 0..999_999_999_999L) { "n must be in 0..999_999_999_999" }
        if (n == 0L) return "zero"
        val below20 = listOf(
            "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
            "seventeen", "eighteen", "nineteen"
        )
        val tens = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")
        fun underThousand(v: Long): String {
            val parts = mutableListOf<String>()
            val h = v / 100
            val rest = v % 100
            if (h > 0) parts.add("${below20[h.toInt()]} hundred")
            if (rest > 0) {
                val tail = if (rest < 20) below20[rest.toInt()]
                else {
                    val t = tens[(rest / 10).toInt()]
                    val o = (rest % 10).toInt()
                    if (o == 0) t else "$t-${below20[o]}"
                }
                parts.add(tail)
            }
            return parts.joinToString(" ")
        }
        val chunks = mutableListOf<String>()
        var rem = n
        val scales = listOf("", "thousand", "million", "billion")
        var scaleIdx = 0
        while (rem > 0) {
            require(scaleIdx < scales.size) { "n must be in 0..999_999_999_999" }
            val cur = rem % 1000
            if (cur > 0) {
                val words = underThousand(cur)
                val scale = scales[scaleIdx]
                chunks.add(if (scale.isEmpty()) words else "$words $scale")
            }
            rem /= 1000
            scaleIdx += 1
        }
        return chunks.reversed().joinToString(" ")
    }

    /**
     * Mirrors the forgiving eval above: trailing operators are stripped and
     * missing closing parens are auto-appended by [eval], so those inputs
     * validate as OK (null). Only extra closing parens (which eval cannot
     * fix) report "Unbalanced brackets", and more than 1000 unclosed opens
     * (which eval rejects) report "Too many unclosed parentheses".
     */
    fun validateExpr(input: String): String? {
        if (input.isBlank()) return "Empty"
        var t = input.trim()
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
        while (t.isNotEmpty() && t.last() in "+-*/^%") {
            t = t.dropLast(1).trimEnd()
        }
        if (t.isBlank()) return "Empty"
        if (Regex("[^0-9a-zA-Z+\\-×÷*/^%().!√πe, −]").containsMatchIn(t)) return "Invalid character"
        val opens = t.count { it == '(' }
        val closes = t.count { it == ')' }
        if (closes > opens) return "Unbalanced brackets"
        if (opens - closes > 1000) return "Too many unclosed parentheses"
        if (Regex("\\(\\s*\\)").containsMatchIn(t)) return "Empty brackets"
        Regex("(\\d+)!").findAll(t).forEach {
            runCatching { it.groupValues[1].toLong() }.getOrNull()?.let { n ->
                if (n > 170L) return "Too large"
            }
        }
        return null
    }

    /**
     * Max fractional digits analyzed by [repeatingToDecimal] and
     * [decimalToFraction]; longer periods/precision throw
     * IllegalArgumentException naming this limit instead of hanging on
     * unbounded BigInteger pow / repetend tracking.
     */
    const val MAX_FRACTION_DIGITS = 12

    fun repeatingToDecimal(num: Long, den: Long): String {
        require(den != 0L) { "denominator must not be zero" }
        if (num == 0L) return "0"
        val neg = (num < 0) != (den < 0)
        val nn = BigInteger.valueOf(num).abs()
        val dd = BigInteger.valueOf(den).abs()
        val intPart = nn.divide(dd).toString()
        var rem = nn.remainder(dd)
        if (rem == BigInteger.ZERO) return (if (neg) "-" else "") + intPart
        val digits = StringBuilder()
        val seen = mutableMapOf<BigInteger, Int>()
        var repeatStart = -1
        while (rem != BigInteger.ZERO) {
            if (digits.length > MAX_FRACTION_DIGITS) {
                throw IllegalArgumentException("repeating decimal period too long: max $MAX_FRACTION_DIGITS digits")
            }
            val prev = seen[rem]
            if (prev != null) {
                repeatStart = prev
                break
            }
            seen[rem] = digits.length
            rem = rem.multiply(BigInteger.TEN)
            val d = rem.divide(dd)
            digits.append(d.toString())
            rem = rem.remainder(dd)
        }
        val prefix = (if (neg) "-" else "") + intPart + "."
        if (rem == BigInteger.ZERO) return prefix + digits.toString()
        return prefix + digits.substring(0, repeatStart) + "(" + digits.substring(repeatStart) + ")"
    }

    fun decimalToFraction(s: String): String {
        val t = s.trim()
        require(t.isNotEmpty()) { "invalid decimal: $s" }
        var body = t
        var sign = 1
        if (body.startsWith("+") || body.startsWith("-")) {
            if (body.startsWith("-")) sign = -1
            body = body.drop(1)
        }
        require(body.isNotEmpty()) { "invalid decimal: $s" }
        if (body.contains("(") || body.contains(")")) {
            require(body.endsWith(")") && body.contains("(") && body.contains(".")) { "invalid repeating decimal: $s" }
            val open = body.indexOf('(')
            val close = body.indexOf(')')
            require(open > 0 && close == body.length - 1 && body.indexOf('(', open + 1) == -1) { "invalid repeating decimal: $s" }
            val rep = body.substring(open + 1, close)
            require(rep.isNotEmpty() && rep.all { it in '0'..'9' }) { "invalid repeating decimal: $s" }
            val beforeParen = body.substring(0, open)
            val dot = beforeParen.indexOf('.')
            require(dot >= 0 && beforeParen.indexOf('.', dot + 1) == -1) { "invalid repeating decimal: $s" }
            val intStr = beforeParen.substring(0, dot)
            val nonRep = beforeParen.substring(dot + 1)
            require(intStr.isNotEmpty() && intStr.all { it in '0'..'9' }) { "invalid repeating decimal: $s" }
            require(nonRep.all { it in '0'..'9' }) { "invalid repeating decimal: $s" }
            require(nonRep.length + rep.length <= MAX_FRACTION_DIGITS) {
                "too many fractional digits: max $MAX_FRACTION_DIGITS"
            }
            if (rep.all { it == '0' }) {
                val frac = nonRep
                if (frac.isEmpty()) {
                    val n = BigInteger(intStr)
                    if (n == BigInteger.ZERO) return "0/1"
                    return (if (sign < 0) "-" else "") + n.toString() + "/1"
                }
                val den = BigInteger.TEN.pow(frac.length)
                var numAbs = BigInteger(intStr + frac)
                if (numAbs == BigInteger.ZERO) return "0/1"
                val g = numAbs.gcd(den)
                numAbs = numAbs.divide(g)
                val d = den.divide(g)
                return (if (sign < 0) "-" else "") + numAbs.toString() + "/" + d.toString()
            }
            val n = nonRep.length
            val r = rep.length
            val pow10n = BigInteger.TEN.pow(n)
            val pow10nr = BigInteger.TEN.pow(n + r)
            val den = pow10nr.subtract(pow10n)
            val aStr = intStr + nonRep + rep
            val bStr = intStr + nonRep
            val a = BigInteger(aStr)
            val b = if (bStr.isEmpty()) BigInteger.ZERO else BigInteger(bStr)
            var numAbs = a.subtract(b)
            if (numAbs == BigInteger.ZERO) return "0/1"
            val g = numAbs.gcd(den)
            numAbs = numAbs.divide(g)
            val d = den.divide(g)
            return (if (sign < 0) "-" else "") + numAbs.toString() + "/" + d.toString()
        }
        if (body.contains('.')) {
            val parts = body.split('.')
            require(parts.size == 2) { "invalid decimal: $s" }
            val intP = parts[0]
            val fracP = parts[1]
            require(intP.isEmpty() || intP.all { it in '0'..'9' }) { "invalid decimal: $s" }
            require(fracP.all { it in '0'..'9' }) { "invalid decimal: $s" }
            require(intP.isNotEmpty() || fracP.isNotEmpty()) { "invalid decimal: $s" }
            require(fracP.length <= MAX_FRACTION_DIGITS) {
                "too many fractional digits: max $MAX_FRACTION_DIGITS"
            }
            val intNorm = if (intP.isEmpty()) "0" else intP
            if (fracP.isEmpty()) {
                val n = BigInteger(intNorm)
                if (n == BigInteger.ZERO) return "0/1"
                return (if (sign < 0) "-" else "") + n.toString() + "/1"
            }
            var numAbs = BigInteger(intNorm + fracP)
            if (numAbs == BigInteger.ZERO) return "0/1"
            val den = BigInteger.TEN.pow(fracP.length)
            val g = numAbs.gcd(den)
            numAbs = numAbs.divide(g)
            val d = den.divide(g)
            return (if (sign < 0) "-" else "") + numAbs.toString() + "/" + d.toString()
        }
        require(body.all { it in '0'..'9' }) { "invalid decimal: $s" }
        val n = BigInteger(body)
        if (n == BigInteger.ZERO) return "0/1"
        return (if (sign < 0) "-" else "") + n.toString() + "/1"
    }

    fun radixConvert(intPart: String, fracPart: String, from: Int, to: Int, cap: Int = 12): String {
        require(from in 2..36) { "from must be in 2..36" }
        require(to in 2..36) { "to must be in 2..36" }
        require(cap >= 0) { "cap must be >= 0" }
        val ip = intPart.trim()
        require(ip.isNotEmpty()) { "invalid intPart: $intPart" }
        var digits = ip
        var neg = false
        if (digits.startsWith("+") || digits.startsWith("-")) {
            neg = digits.startsWith("-")
            digits = digits.drop(1)
            require(digits.isNotEmpty()) { "invalid intPart: $intPart" }
        }
        require(digits.all { Character.digit(it, from) >= 0 }) { "invalid digit for base $from: $intPart" }
        val fp = fracPart.trim()
        require(fp.all { Character.digit(it, from) >= 0 }) { "invalid fracPart: $fracPart" }
        val absInt = if (digits.all { it == '0' }) BigInteger.ZERO else BigInteger(digits, from)
        val intStr = absInt.toString(to).uppercase()
        val intIsZero = absInt == BigInteger.ZERO
        if (fp.isEmpty() || cap == 0) {
            if (intIsZero) return "0"
            return (if (neg) "-" else "") + intStr
        }
        var num = BigInteger.ZERO
        if (fp.any { it != '0' }) {
            num = BigInteger(fp, from)
        }
        if (num == BigInteger.ZERO) {
            if (intIsZero) return "0"
            return (if (neg) "-" else "") + intStr
        }
        val den = BigInteger.valueOf(from.toLong()).pow(fp.length)
        val sb = StringBuilder()
        var cur = num
        for (i in 0 until cap) {
            cur = cur.multiply(BigInteger.valueOf(to.toLong()))
            val q = cur.divide(den)
            sb.append(Character.forDigit(q.toInt(), to).uppercaseChar())
            cur = cur.remainder(den)
            if (cur == BigInteger.ZERO) break
        }
        if (sb.isEmpty()) {
            if (intIsZero) return "0"
            return (if (neg) "-" else "") + intStr
        }
        val signed = (if (neg) "-" else "") + intStr
        return "$signed.${sb}"
    }

    fun formatPercentMode(value: Double, mode: String): String {
        require(value.isFinite()) { "value must be finite" }
        val m = mode.trim().lowercase()
        val factor = when (m) {
            "percent" -> BigDecimal(100)
            "permille" -> BigDecimal(1000)
            "permyriad" -> BigDecimal(10000)
            else -> throw IllegalArgumentException("unknown mode: $mode")
        }
        val suffix = when (m) {
            "percent" -> "%"
            "permille" -> "‰"
            "permyriad" -> "‱"
            else -> throw IllegalArgumentException("unknown mode: $mode")
        }
        val scaled = BigDecimal.valueOf(value).multiply(factor).stripTrailingZeros()
        if (scaled.compareTo(BigDecimal.ZERO) == 0) return "0$suffix"
        return scaled.toPlainString() + suffix
    }
}
