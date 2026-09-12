package calc.u.core

import java.security.SecureRandom

object FunTools {
    fun randomColor(rng: SecureRandom = SecureRandom()): Triple<Int, Int, Int> {
        return Triple(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256))
    }

    fun toHex(rgb: Triple<Int, Int, Int>): String {
        val (r, g, b) = rgb
        return toHex(r, g, b)
    }

    fun toHex(r: Int, g: Int, b: Int): String {
        require(r in 0..255) { "r must be in 0..255" }
        require(g in 0..255) { "g must be in 0..255" }
        require(b in 0..255) { "b must be in 0..255" }
        return "#%02X%02X%02X".format(r, g, b)
    }

    fun symbolsTable(): List<Pair<String, String>> = listOf(
        "Alpha" to "Α",
        "Beta" to "Β",
        "Gamma" to "Γ",
        "Delta" to "Δ",
        "Theta" to "Θ",
        "Lambda" to "Λ",
        "Pi" to "Π",
        "Sigma" to "Σ",
        "Omega" to "Ω",
        "alpha" to "α",
        "beta" to "β",
        "gamma" to "γ",
        "delta" to "δ",
        "theta" to "θ",
        "lambda" to "λ",
        "pi" to "π",
        "sigma" to "σ",
        "omega" to "ω",
        "euro" to "€",
        "pound" to "£",
        "yen" to "¥",
        "rupee" to "₹",
        "won" to "₩",
        "lira" to "₺",
        "dollar" to "\$",
        "cent" to "¢",
        "plus-minus" to "±",
        "multiply" to "×",
        "divide" to "÷",
        "square root" to "√",
        "infinity" to "∞",
        "summation" to "∑",
        "integral" to "∫",
        "not equal" to "≠",
        "approximately" to "≈",
        "less or equal" to "≤",
        "greater or equal" to "≥",
        "degree" to "°",
        "right arrow" to "→",
        "left arrow" to "←",
        "up arrow" to "↑",
        "down arrow" to "↓",
        "left-right arrow" to "↔",
        "up-down arrow" to "↕",
        "check mark" to "✓",
        "cross mark" to "✕",
        "star" to "★",
        "heart" to "♥",
        "copyright" to "©",
        "registered" to "®",
        "trademark" to "™",
        "section" to "§",
        "paragraph" to "¶",
        "male" to "♂",
        "female" to "♀",
        "bullet" to "•",
        "em dash" to "—",
        "ellipsis" to "…",
        "inverted question" to "¿",
        "inverted exclamation" to "¡"
    )

    fun roulettePick(items: List<String>, seed: Long? = null): String {
        require(items.size in 2..8) { "items must have 2..8 entries" }
        val rng = SecureRandom()
        if (seed != null) rng.setSeed(seed)
        return items[rng.nextInt(items.size)]
    }

    fun rangeRandom(min: Int, max: Int): Int {
        require(min <= max) { "min must be <= max" }
        require(min in -999..9999) { "min must be in -999..9999" }
        require(max in -999..9999) { "max must be in -999..9999" }
        val rng = SecureRandom()
        val span = max - min + 1
        return min + rng.nextInt(span)
    }

    private const val LETTERS_FULL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS_FULL = "0123456789"
    private const val SPECIALS_FULL = "!@#\$%^&*()-_=+[]{};:,.<>?/~"
    private const val AMBIGUOUS = "Il1O0"

    fun passwordPlus(length: Int, digits: Int, specials: Int, ambiguous: Boolean = false): String {
        require(length in 1..64) { "length must be in 1..64" }
        require(digits >= 0) { "digits must be >= 0" }
        require(specials >= 0) { "specials must be >= 0" }
        require(length >= digits + specials) { "length must be >= digits + specials" }
        val letters = if (ambiguous) LETTERS_FULL else LETTERS_FULL.filter { it !in AMBIGUOUS }
        val digitPool = if (ambiguous) DIGITS_FULL else DIGITS_FULL.filter { it !in AMBIGUOUS }
        val specialsPool = SPECIALS_FULL
        val rng = SecureRandom()
        val out = CharArray(length)
        var i = 0
        repeat(digits) { out[i++] = digitPool[rng.nextInt(digitPool.length)] }
        repeat(specials) { out[i++] = specialsPool[rng.nextInt(specialsPool.length)] }
        while (i < length) {
            out[i++] = letters[rng.nextInt(letters.length)]
        }
        for (j in out.size - 1 downTo 1) {
            val k = rng.nextInt(j + 1)
            val t = out[j]
            out[j] = out[k]
            out[k] = t
        }
        return String(out)
    }
}
