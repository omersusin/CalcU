package calc.u.core

object NumberTheory {
    fun totient(n: Long): Long {
        require(n > 0) { "n must be > 0" }
        var result = n
        var x = n
        var p = 2L
        while (p * p <= x) {
            if (x % p == 0L) {
                while (x % p == 0L) x /= p
                result -= result / p
            }
            p += if (p == 2L) 1L else 2L
        }
        if (x > 1) result -= result / x
        return result
    }

    fun modInverse(a: Long, m: Long): Long {
        require(m > 1) { "m must be > 1" }
        var t = 0L
        var newT = 1L
        var r = m
        var newR = ((a % m) + m) % m
        while (newR != 0L) {
            val q = r / newR
            val tmpT = t - q * newT
            t = newT
            newT = tmpT
            val tmpR = r - q * newR
            r = newR
            newR = tmpR
        }
        require(r == 1L) { "no modular inverse for $a mod $m" }
        if (t < 0) t += m
        return t
    }

    fun primeFactors(n: Long): List<Long> {
        require(n >= 2) { "n must be >= 2" }
        val out = ArrayList<Long>()
        var x = n
        while (x % 2L == 0L) {
            out.add(2L)
            x /= 2L
        }
        var p = 3L
        while (p * p <= x) {
            while (x % p == 0L) {
                out.add(p)
                x /= p
            }
            p += 2L
        }
        if (x > 1) out.add(x)
        return out
    }

    fun fibonacci(n: Int): Long {
        require(n in 0..92) { "n must be in 0..92" }
        return fibPair(n.toLong()).first
    }

    private fun fibPair(n: Long): Pair<Long, Long> {
        if (n == 0L) return Pair(0L, 1L)
        val (a, b) = fibPair(n / 2)
        val c = a * (2 * b - a)
        val d = a * a + b * b
        return if (n % 2 == 0L) Pair(c, d) else Pair(d, c + d)
    }

    fun collatzSteps(n: Long): Int {
        require(n > 0) { "n must be > 0" }
        var x = n
        var steps = 0
        while (x != 1L) {
            x = if (x % 2L == 0L) x / 2 else 3 * x + 1
            steps++
        }
        return steps
    }
}
