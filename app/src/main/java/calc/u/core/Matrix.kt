package calc.u.core

class Matrix(val rows: Int, val cols: Int, private val d: DoubleArray) {
    init {
        require(rows in 1..4 && cols in 1..4) { "matrix size must be 1..4 x 1..4, got ${rows}x${cols}" }
        require(d.size == rows * cols) { "data size ${d.size} does not match ${rows}x${cols}" }
    }

    operator fun get(r: Int, c: Int): Double {
        require(r in 0 until rows && c in 0 until cols) { "index ($r,$c) out of bounds for ${rows}x${cols}" }
        return d[r * cols + c]
    }

    fun add(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "add shape mismatch: ${rows}x${cols} vs ${other.rows}x${other.cols}" }
        return Matrix(rows, cols, DoubleArray(d.size) { d[it] + other.d[it] })
    }

    fun multiply(other: Matrix): Matrix {
        require(cols == other.rows) { "multiply shape mismatch: ${rows}x${cols} vs ${other.rows}x${other.cols}" }
        val out = DoubleArray(rows * other.cols)
        for (i in 0 until rows) for (j in 0 until other.cols) {
            var s = 0.0
            for (k in 0 until cols) s += get(i, k) * other[k, j]
            out[i * other.cols + j] = s
        }
        return Matrix(rows, other.cols, out)
    }

    fun scalarMultiply(s: Double): Matrix =
        Matrix(rows, cols, DoubleArray(d.size) { d[it] * s })

    fun transpose(): Matrix {
        val out = DoubleArray(d.size)
        for (r in 0 until rows) for (c in 0 until cols) out[c * rows + r] = get(r, c)
        return Matrix(cols, rows, out)
    }

    fun determinant(): Double {
        require(rows == cols) { "determinant requires square matrix, got ${rows}x${cols}" }
        return det(toGrid())
    }

    fun inverse(): Matrix {
        require(rows == cols) { "inverse requires square matrix, got ${rows}x${cols}" }
        val det = determinant()
        require(det != 0.0 && det.isFinite()) { "matrix is singular, cannot invert" }
        if (rows == 1) return Matrix(1, 1, doubleArrayOf(1.0 / det))
        val grid = toGrid()
        val cof = List(rows) { r ->
            List(cols) { c ->
                val minor = minor(grid, r, c)
                val sign = if ((r + c) % 2 == 0) 1.0 else -1.0
                sign * det(minor)
            }
        }
        val out = DoubleArray(d.size)
        for (r in 0 until rows) for (c in 0 until cols) out[r * cols + c] = cof[c][r] / det
        return Matrix(rows, cols, out)
    }

    fun pretty(): String {
        val rowsStr = (0 until rows).map { r ->
            (0 until cols).map { c -> fmtNum(get(r, c)) }.joinToString(", ")
        }
        return rowsStr.joinToString("\n", "[", "]")
    }

    private fun toGrid(): List<List<Double>> =
        List(rows) { r -> List(cols) { c -> get(r, c) } }

    companion object {
        fun of(rows: Int, cols: Int, vararg values: Double): Matrix {
            require(values.size == rows * cols) { "expected ${rows * cols} values, got ${values.size}" }
            return Matrix(rows, cols, values.copyOf())
        }

        fun of2x2(a11: Double, a12: Double, a21: Double, a22: Double): Matrix =
            Matrix(2, 2, doubleArrayOf(a11, a12, a21, a22))

        fun identity(n: Int): Matrix {
            require(n in 1..4) { "identity size must be 1..4, got $n" }
            val out = DoubleArray(n * n)
            for (i in 0 until n) out[i * n + i] = 1.0
            return Matrix(n, n, out)
        }

        private fun det(m: List<List<Double>>): Double {
            val n = m.size
            require(n > 0 && m.all { it.size == n }) { "determinant requires square matrix" }
            if (n == 1) return m[0][0]
            if (n == 2) return m[0][0] * m[1][1] - m[0][1] * m[1][0]
            var s = 0.0
            for (c in m[0].indices) s += m[0][c] * cofactor(m, 0, c)
            return s
        }

        private fun cofactor(m: List<List<Double>>, r: Int, c: Int): Double {
            val sign = if ((r + c) % 2 == 0) 1.0 else -1.0
            return sign * det(minor(m, r, c))
        }

        private fun minor(m: List<List<Double>>, skipR: Int, skipC: Int): List<List<Double>> =
            m.filterIndexed { r, _ -> r != skipR }
                .map { row -> row.filterIndexed { c, _ -> c != skipC } }

        private fun fmtNum(v: Double): String {
            if (!v.isFinite()) return "—"
            if (v == 0.0) return "0"
            return java.math.BigDecimal.valueOf(v).stripTrailingZeros().toPlainString()
        }
    }
}
