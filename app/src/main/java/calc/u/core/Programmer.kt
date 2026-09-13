package calc.u.core

/**
 * Programmer-mode bit operations on [Long].
 *
 * yetCalc reference semantics (ProgrammerOps): NAND = (a & b).inv(),
 * NOR = (a | b).inv(), XNOR = (a xor b).inv(), LSH = a shl b,
 * RSH = a shr b, URSH = a ushr b, RoL/RoR = 64-bit rotates.
 * This object keeps those identities for width 64 and extends them to
 * width-masked 8/16/32-bit modes.
 *
 * Integer only: base conversion handles whole values; fractional input
 * is rejected with IllegalArgumentException (documented, no silent truncation).
 */
object Programmer {
    enum class BitWidth(val bits: Int) {
        W8(8),
        W16(16),
        W32(32),
        W64(64);

        fun mask(): Long = when (bits) {
            64 -> -1L
            else -> (1L shl bits) - 1L
        }

        companion object {
            fun of(bits: Int): BitWidth = runCatching {
                entries.first { it.bits == bits }
            }.getOrElse {
                throw IllegalArgumentException("bit width must be one of 8, 16, 32, 64 (got $bits)")
            }
        }
    }

    fun maskOf(width: BitWidth): Long = width.mask()

    fun mask(value: Long, width: BitWidth): Long = value and width.mask()

    /**
     * Two's-complement signed interpretation of [value] under [width].
     * Masks first, then sign-extends. Width 64 returns the value unchanged.
     */
    fun signed(value: Long, width: BitWidth): Long {
        val m = mask(value, width)
        if (width == BitWidth.W64) return m
        val signBit = 1L shl (width.bits - 1)
        return if ((m and signBit) != 0L) m - (1L shl width.bits) else m
    }

    /** Unsigned (zero-extended) interpretation: just the masked value. */
    fun unsigned(value: Long, width: BitWidth): Long = mask(value, width)

    fun and(a: Long, b: Long, width: BitWidth = BitWidth.W64): Long =
        mask(a and b, width)

    fun or(a: Long, b: Long, width: BitWidth = BitWidth.W64): Long =
        mask(a or b, width)

    fun xor(a: Long, b: Long, width: BitWidth = BitWidth.W64): Long =
        mask(a xor b, width)

    fun nand(a: Long, b: Long, width: BitWidth = BitWidth.W64): Long =
        mask((a and b).inv(), width)

    fun nor(a: Long, b: Long, width: BitWidth = BitWidth.W64): Long =
        mask((a or b).inv(), width)

    fun xnor(a: Long, b: Long, width: BitWidth = BitWidth.W64): Long =
        mask((a xor b).inv(), width)

    fun not(a: Long, width: BitWidth = BitWidth.W64): Long =
        mask(a.inv(), width)

    private fun checkShift(bits: Int): Int {
        require(bits >= 0) { "shift must be >= 0 (got $bits)" }
        return bits
    }

    /** Logical left shift; shifts >= width yield 0 (no JVM masking surprise). */
    fun shl(a: Long, bits: Int, width: BitWidth = BitWidth.W64): Long {
        checkShift(bits)
        if (bits >= width.bits) return 0L
        return mask(mask(a, width) shl bits, width)
    }

    /** Arithmetic right shift on the signed interpretation; shifts >= width sign-fill. */
    fun shr(a: Long, bits: Int, width: BitWidth = BitWidth.W64): Long {
        checkShift(bits)
        if (bits >= width.bits) return if (signed(a, width) < 0) maskOf(width) else 0L
        return mask(signed(a, width) shr bits, width)
    }

    /** Logical (unsigned) right shift; shifts >= width yield 0. */
    fun ushr(a: Long, bits: Int, width: BitWidth = BitWidth.W64): Long {
        checkShift(bits)
        if (bits >= width.bits) return 0L
        return mask(mask(a, width) ushr bits, width)
    }

    /** Rotate left within [width]; distance normalized mod width. */
    fun rol(a: Long, bits: Int, width: BitWidth = BitWidth.W64): Long {
        checkShift(bits)
        val v = mask(a, width)
        if (width == BitWidth.W64) return java.lang.Long.rotateLeft(v, bits)
        val k = bits % width.bits
        if (k == 0) return v
        return mask((v shl k) or (v ushr (width.bits - k)), width)
    }

    /** Rotate right within [width]; distance normalized mod width. */
    fun ror(a: Long, bits: Int, width: BitWidth = BitWidth.W64): Long {
        checkShift(bits)
        val v = mask(a, width)
        if (width == BitWidth.W64) return java.lang.Long.rotateRight(v, bits)
        val k = bits % width.bits
        if (k == 0) return v
        return mask((v ushr k) or (v shl (width.bits - k)), width)
    }

    /**
     * Format [value] in [base] (2..36). Integer only: fractional values
     * have no representation here. With [width], formats the masked
     * (unsigned) value; otherwise formats the signed Long directly.
     */
    fun toBase(value: Long, base: Int, width: BitWidth? = null): String {
        require(base in 2..36) { "base must be 2..36 (got $base)" }
        val v = if (width == null) value else mask(value, width)
        return v.toString(base).uppercase()
    }

    /**
     * Parse integer text in [base] (2..36). Accepts leading +/- and
     * 0x/0b/0o prefixes for bases 16/2/8. Rejects fractional text
     * (".", "e" exponent markers are not accepted as integers) with
     * IllegalArgumentException — fractional conversion is unsupported.
     */
    fun parse(text: String, base: Int): Long {
        require(base in 2..36) { "base must be 2..36 (got $base)" }
        val t = text.trim()
        require(t.isNotEmpty()) { "empty input" }
        require('.' !in t && 'e' !in t.lowercase()) {
            "fractional input not supported (integer only): \"$text\""
        }
        val neg = t.startsWith("-")
        val pos = t.startsWith("+")
        var body = when {
            neg || pos -> t.drop(1)
            else -> t
        }
        require(body.isNotEmpty()) { "empty input" }
        if (base == 16 && body.startsWith("0x", ignoreCase = true)) body = body.drop(2)
        if (base == 2 && body.startsWith("0b", ignoreCase = true)) body = body.drop(2)
        if (base == 8 && body.startsWith("0o", ignoreCase = true)) body = body.drop(2)
        require(body.isNotEmpty()) { "empty input" }
        val digits = if (neg) "-$body" else body
        return digits.toLongOrNull(base)
            ?: throw IllegalArgumentException("invalid base-$base integer: \"$text\"")
    }

    /** Binary string padded to [width] bits (two's-complement view). */
    fun toBinaryString(value: Long, width: BitWidth): String {
        val m = mask(value, width)
        val raw = m.toString(2)
        return raw.padStart(width.bits, '0')
    }

    data class Views(
        val signedDec: String,
        val unsignedDec: String,
        val hex: String,
        val oct: String,
        val bin: String,
        val ascii: String,
        val codePoint: String?
    )

    /** Multi-base + ASCII/Unicode snapshot of [value] under [width]. */
    fun views(value: Long, width: BitWidth): Views {
        val m = mask(value, width)
        return Views(
            signedDec = signed(value, width).toString(),
            unsignedDec = m.toString(),
            hex = m.toString(16).uppercase().padStart(width.bits / 4, '0'),
            oct = m.toString(8),
            bin = toBinaryString(value, width),
            ascii = asciiView(value),
            codePoint = unicodeChar(value)
        )
    }

    /**
     * ASCII view: 8 big-endian bytes of the Long; printable 32..126 kept,
     * anything else becomes '.'.
     */
    fun asciiView(value: Long): String {
        val sb = StringBuilder(8)
        for (i in 7 downTo 0) {
            val b = ((value ushr (i * 8)) and 0xFF).toInt()
            sb.append(if (b in 32..126) b.toChar() else '.')
        }
        return sb.toString()
    }

    /**
     * Unicode scalar view of the low 21+ bits: returns the character for
     * valid non-surrogate code points, null when out of range.
     */
    fun unicodeChar(value: Long): String? {
        if (value < 0 || value > 0x10FFFF) return null
        val cp = value.toInt()
        if (cp in 0xD800..0xDFFF) return null
        return runCatching { String(Character.toChars(cp)) }.getOrNull()
    }
}
