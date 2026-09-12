package calc.u.core

import kotlin.math.pow

object UnitExpr {
    data class Quantity(val factor: Double, val dim: IntArray) {
        override fun equals(other: Any?): Boolean =
            other is Quantity && factor == other.factor && dim.contentEquals(other.dim)

        override fun hashCode(): Int = 31 * factor.hashCode() + dim.contentHashCode()

        override fun toString(): String = "Quantity($factor, [${dim.joinToString(", ")}])"
    }

    private data class UnitDef(val factor: Double, val dim: IntArray)

    private fun d(
        l: Int = 0,
        m: Int = 0,
        t: Int = 0,
        i: Int = 0,
        th: Int = 0,
        n: Int = 0,
        j: Int = 0
    ): IntArray = intArrayOf(l, m, t, i, th, n, j)

    private val units: Map<String, UnitDef> = mapOf(
        "m" to UnitDef(1.0, d(l = 1)),
        "km" to UnitDef(1000.0, d(l = 1)),
        "cm" to UnitDef(0.01, d(l = 1)),
        "mm" to UnitDef(0.001, d(l = 1)),
        "in" to UnitDef(0.0254, d(l = 1)),
        "ft" to UnitDef(0.3048, d(l = 1)),
        "yd" to UnitDef(0.9144, d(l = 1)),
        "mi" to UnitDef(1609.344, d(l = 1)),
        "mil" to UnitDef(2.54e-5, d(l = 1)),
        "hand" to UnitDef(0.1016, d(l = 1)),
        "link" to UnitDef(0.201168, d(l = 1)),
        "chain" to UnitDef(20.1168, d(l = 1)),
        "rod" to UnitDef(5.0292, d(l = 1)),
        "fathom" to UnitDef(1.8288, d(l = 1)),
        "point" to UnitDef(3.527777777777778e-4, d(l = 1)),
        "pica" to UnitDef(0.004233333333333333, d(l = 1)),
        "kg" to UnitDef(1.0, d(m = 1)),
        "g" to UnitDef(0.001, d(m = 1)),
        "mg" to UnitDef(1e-6, d(m = 1)),
        "lb" to UnitDef(0.45359237, d(m = 1)),
        "oz" to UnitDef(0.028349523125, d(m = 1)),
        "t" to UnitDef(1000.0, d(m = 1)),
        "s" to UnitDef(1.0, d(t = 1)),
        "min" to UnitDef(60.0, d(t = 1)),
        "hr" to UnitDef(3600.0, d(t = 1)),
        "day" to UnitDef(86400.0, d(t = 1)),
        "week" to UnitDef(604800.0, d(t = 1)),
        "fortnight" to UnitDef(1209600.0, d(t = 1)),
        "yr" to UnitDef(31556925.9746784, d(t = 1)),
        "ms" to UnitDef(0.001, d(t = 1)),
        "N" to UnitDef(1.0, d(l = 1, m = 1, t = -2)),
        "lbf" to UnitDef(4.4482216152605, d(l = 1, m = 1, t = -2)),
        "dyn" to UnitDef(1e-5, d(l = 1, m = 1, t = -2)),
        "kgf" to UnitDef(9.80665, d(l = 1, m = 1, t = -2)),
        "J" to UnitDef(1.0, d(l = 2, m = 1, t = -2)),
        "kJ" to UnitDef(1000.0, d(l = 2, m = 1, t = -2)),
        "cal" to UnitDef(4.184, d(l = 2, m = 1, t = -2)),
        "kcal" to UnitDef(4184.0, d(l = 2, m = 1, t = -2)),
        "kWh" to UnitDef(3.6e6, d(l = 2, m = 1, t = -2)),
        "Btu" to UnitDef(1055.05585262, d(l = 2, m = 1, t = -2)),
        "therm" to UnitDef(105505585.262, d(l = 2, m = 1, t = -2)),
        "tonTNT" to UnitDef(4.184e9, d(l = 2, m = 1, t = -2)),
        "eV" to UnitDef(1.602176634e-19, d(l = 2, m = 1, t = -2)),
        "W" to UnitDef(1.0, d(l = 2, m = 1, t = -3)),
        "kW" to UnitDef(1000.0, d(l = 2, m = 1, t = -3)),
        "hp" to UnitDef(745.6998715822702, d(l = 2, m = 1, t = -3)),
        "Pa" to UnitDef(1.0, d(l = -1, m = 1, t = -2)),
        "kPa" to UnitDef(1000.0, d(l = -1, m = 1, t = -2)),
        "bar" to UnitDef(1e5, d(l = -1, m = 1, t = -2)),
        "psi" to UnitDef(6894.757293178, d(l = -1, m = 1, t = -2)),
        "ksi" to UnitDef(6894757.293178, d(l = -1, m = 1, t = -2)),
        "inHg" to UnitDef(3386.389, d(l = -1, m = 1, t = -2)),
        "torr" to UnitDef(133.322368, d(l = -1, m = 1, t = -2)),
        "atm" to UnitDef(101325.0, d(l = -1, m = 1, t = -2)),
        "V" to UnitDef(1.0, d(l = 2, m = 1, t = -3, i = -1)),
        "A" to UnitDef(1.0, d(i = 1)),
        "ohm" to UnitDef(1.0, d(l = 2, m = 1, t = -3, i = -2)),
        "C" to UnitDef(1.0, d(t = 1, i = 1)),
        "K" to UnitDef(1.0, d(th = 1)),
        "mol" to UnitDef(1.0, d(n = 1)),
        "cd" to UnitDef(1.0, d(j = 1)),
        "L" to UnitDef(0.001, d(l = 3)),
        "mL" to UnitDef(1e-6, d(l = 3)),
        "gal" to UnitDef(0.003785411784, d(l = 3)),
        "qt" to UnitDef(9.46352946e-4, d(l = 3)),
        "pt" to UnitDef(4.73176473e-4, d(l = 3)),
        "floz" to UnitDef(2.95735e-5, d(l = 3)),
        "fldram" to UnitDef(3.6966875e-6, d(l = 3)),
        "tbsp_metric" to UnitDef(1.5e-5, d(l = 3)),
        "mph" to UnitDef(0.44704, d(l = 1, t = -1)),
        "knot" to UnitDef(0.5144444444444445, d(l = 1, t = -1)),
        "kph" to UnitDef(1.0 / 3.6, d(l = 1, t = -1)),
        "rad" to UnitDef(1.0, d()),
        "deg" to UnitDef(0.017453292519943295, d()),
        "turn" to UnitDef(6.283185307179586, d()),
        "rev" to UnitDef(6.283185307179586, d()),
        "grad" to UnitDef(0.015707963267948967, d()),
        "arcmin" to UnitDef(2.908882086657216e-4, d()),
        "arcsec" to UnitDef(4.84813681109536e-6, d())
    )

    private val aliases: Map<String, String> = mapOf(
        "meter" to "m", "meters" to "m", "metre" to "m", "metres" to "m",
        "kilometer" to "km", "kilometers" to "km", "kilometre" to "km", "kilometres" to "km",
        "centimeter" to "cm", "centimeters" to "cm", "centimetre" to "cm", "centimetres" to "cm",
        "millimeter" to "mm", "millimeters" to "mm", "millimetre" to "mm", "millimetres" to "mm",
        "inch" to "in", "inches" to "in",
        "foot" to "ft", "feet" to "ft",
        "yard" to "yd", "yards" to "yd",
        "mile" to "mi", "miles" to "mi",
        "mils" to "mil",
        "hands" to "hand",
        "links" to "link",
        "chains" to "chain",
        "rods" to "rod",
        "fathoms" to "fathom",
        "points" to "point",
        "picas" to "pica",
        "kilogram" to "kg", "kilograms" to "kg", "kilo" to "kg", "kilos" to "kg",
        "gram" to "g", "grams" to "g",
        "milligram" to "mg", "milligrams" to "mg",
        "pound" to "lb", "pounds" to "lb", "lbs" to "lb", "lbm" to "lb",
        "ounce" to "oz", "ounces" to "oz",
        "tonne" to "t", "tonnes" to "t", "ton" to "t", "tons" to "t",
        "second" to "s", "seconds" to "s", "sec" to "s", "secs" to "s",
        "minute" to "min", "minutes" to "min",
        "hour" to "hr", "hours" to "hr", "h" to "hr",
        "days" to "day",
        "weeks" to "week", "wk" to "week", "wks" to "week",
        "fortnights" to "fortnight",
        "year" to "yr", "years" to "yr",
        "millisecond" to "ms", "milliseconds" to "ms",
        "newton" to "N", "newtons" to "N",
        "dyne" to "dyn", "dynes" to "dyn",
        "joule" to "J", "joules" to "J",
        "kilojoule" to "kJ", "kilojoules" to "kJ",
        "calorie" to "cal", "calories" to "cal",
        "kilocalorie" to "kcal", "kilocalories" to "kcal",
        "kilowatthour" to "kWh", "kilowatthours" to "kWh",
        "britishthermalunit" to "Btu", "britishthermalunits" to "Btu", "btus" to "Btu",
        "therms" to "therm",
        "tontnt" to "tonTNT", "tonsoftnt" to "tonTNT",
        "electronvolt" to "eV", "electronvolts" to "eV",
        "watt" to "W", "watts" to "W",
        "kilowatt" to "kW", "kilowatts" to "kW",
        "horsepower" to "hp",
        "pascal" to "Pa", "pascals" to "Pa",
        "kilopascal" to "kPa", "kilopascals" to "kPa",
        "ksis" to "ksi",
        "inchhg" to "inHg", "incheshg" to "inHg",
        "torrs" to "torr",
        "bars" to "bar",
        "atmosphere" to "atm", "atmospheres" to "atm",
        "volt" to "V", "volts" to "V",
        "ampere" to "A", "amperes" to "A", "amp" to "A", "amps" to "A",
        "ohms" to "ohm", "ω" to "ohm",
        "coulomb" to "C", "coulombs" to "C",
        "kelvin" to "K", "kelvins" to "K",
        "mole" to "mol", "moles" to "mol",
        "candela" to "cd", "candelas" to "cd",
        "liter" to "L", "liters" to "L", "litre" to "L", "litres" to "L", "l" to "L",
        "milliliter" to "mL", "milliliters" to "mL", "millilitre" to "mL", "millilitres" to "mL", "ml" to "mL",
        "gallon" to "gal", "gallons" to "gal",
        "quart" to "qt", "quarts" to "qt",
        "pint" to "pt", "pints" to "pt",
        "fluidounce" to "floz", "fluidounces" to "floz", "fl_oz" to "floz",
        "fluidram" to "fldram", "fluidrams" to "fldram",
        "fluiddram" to "fldram", "fluiddrams" to "fldram", "fl_dram" to "fldram",
        "tbspmetric" to "tbsp_metric", "metrictablespoon" to "tbsp_metric",
        "radian" to "rad", "radians" to "rad",
        "degree" to "deg", "degrees" to "deg",
        "turns" to "turn", "circle" to "turn", "circles" to "turn",
        "revolution" to "turn", "revolutions" to "turn", "revs" to "rev",
        "grads" to "grad", "gradian" to "grad", "gradians" to "grad",
        "arcminute" to "arcmin", "arcminutes" to "arcmin",
        "arcsecond" to "arcsec", "arcseconds" to "arcsec",
        "knots" to "knot", "kt" to "knot",
        "mileperhour" to "mph", "milesperhour" to "mph",
        "kilometerperhour" to "kph", "kilometreperhour" to "kph"
    )

    private val lowerIndex: Map<String, String> =
        units.keys.groupBy({ it.lowercase() }, { it }).mapValues { it.value.first() }

    private fun resolve(name: String): UnitDef {
        units[name]?.let { return it }
        val lower = name.lowercase()
        lowerIndex[lower]?.let { return units[it]!! }
        aliases[lower]?.let { units[it]?.let { u -> return u } }
        val singular = when {
            lower.endsWith("es") -> lower.dropLast(2)
            lower.endsWith("s") -> lower.dropLast(1)
            else -> lower
        }
        if (singular != lower) {
            lowerIndex[singular]?.let { return units[it]!! }
            aliases[singular]?.let { units[it]?.let { u -> return u } }
        }
        throw IllegalArgumentException("unknown unit: $name")
    }

    private sealed interface Token {
        data class Name(val text: String) : Token
        data class Number(val value: Double, val text: String) : Token
        data object Star : Token
        data object Slash : Token
        data object Caret : Token
        data object LParen : Token
        data object RParen : Token
        data object Minus : Token
    }

    private fun normalize(input: String): String {
        var s = input
            .replace("·", "*").replace("•", "*").replace("×", "*")
            .replace("÷", "/").replace("−", "-")
            .replace("²", "^2").replace("³", "^3").replace("¹", "^1")
        // ConvertAll-style multi-word spellings collapse to single tokens
        // (tokenizer splits on whitespace with implicit multiplication).
        s = s.replace(Regex("fluid[ -]?drams?", RegexOption.IGNORE_CASE), "fldram")
        s = s.replace(Regex("fluid[ -]?ounces?", RegexOption.IGNORE_CASE), "floz")
        s = s.replace(Regex("fl[ -]?oz", RegexOption.IGNORE_CASE), "floz")
        s = s.replace(Regex("in(ch)?[ -]?hg", RegexOption.IGNORE_CASE), "inHg")
        s = s.replace(Regex("tons?\\s+tnt", RegexOption.IGNORE_CASE), "tonTNT")
        s = s.replace(Regex("([A-Za-z_)Ωµμ])\\s*(\\d+)"), "\$1^\$2")
        return s
    }

    private fun tokenize(input: String): List<Token> {
        val out = ArrayList<Token>()
        var k = 0
        while (k < input.length) {
            val c = input[k]
            when {
                c.isWhitespace() -> k++
                c == '*' -> { out.add(Token.Star); k++ }
                c == '/' -> { out.add(Token.Slash); k++ }
                c == '^' -> { out.add(Token.Caret); k++ }
                c == '(' -> { out.add(Token.LParen); k++ }
                c == ')' -> { out.add(Token.RParen); k++ }
                c == '-' -> { out.add(Token.Minus); k++ }
                c.isLetter() || c == '_' -> {
                    var j = k + 1
                    while (j < input.length && (input[j].isLetter() || input[j] == '_')) j++
                    out.add(Token.Name(input.substring(k, j)))
                    k = j
                }
                c.isDigit() || c == '.' -> {
                    var j = k
                    while (j < input.length && (input[j].isDigit() || input[j] == '.')) j++
                    if (j < input.length && (input[j] == 'e' || input[j] == 'E')) {
                        var m = j + 1
                        if (m < input.length && (input[m] == '+' || input[m] == '-')) m++
                        val digits = m
                        while (m < input.length && input[m].isDigit()) m++
                        if (m > digits) j = m
                    }
                    val text = input.substring(k, j)
                    out.add(Token.Number(text.toDoubleOrNull() ?: throw IllegalArgumentException("bad number: $text"), text))
                    k = j
                }
                else -> throw IllegalArgumentException("unexpected character: '$c'")
            }
        }
        return out
    }

    private fun mul(a: Quantity, b: Quantity): Quantity {
        val dim = IntArray(7) { a.dim[it] + b.dim[it] }
        return Quantity(a.factor * b.factor, dim)
    }

    private fun div(a: Quantity, b: Quantity): Quantity {
        val dim = IntArray(7) { a.dim[it] - b.dim[it] }
        return Quantity(a.factor / b.factor, dim)
    }

    private fun powQ(a: Quantity, exp: Int): Quantity {
        val dim = IntArray(7) { a.dim[it] * exp }
        return Quantity(a.factor.pow(exp), dim)
    }

    private class Parser(private val tokens: List<Token>) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun next(): Token =
            tokens.getOrNull(pos++) ?: throw IllegalArgumentException("unexpected end of expression")

        fun parse(): Quantity {
            if (tokens.isEmpty()) throw IllegalArgumentException("empty expression")
            val q = parseExpr()
            if (pos != tokens.size) throw IllegalArgumentException("unexpected trailing input: ${tokens[pos]}")
            return q
        }

        private fun parseExpr(): Quantity {
            var acc = parseTerm()
            while (true) {
                when (peek()) {
                    is Token.Star -> { next(); acc = mul(acc, parseTerm()) }
                    is Token.Slash -> { next(); acc = div(acc, parseTerm()) }
                    is Token.Name, is Token.LParen, is Token.Number -> acc = mul(acc, parseTerm())
                    else -> return acc
                }
            }
        }

        private fun parseTerm(): Quantity {
            var base = parseUnary()
            if (peek() is Token.Caret) {
                next()
                base = powQ(base, parseExponent())
            }
            return base
        }

        private fun parseExponent(): Int {
            var neg = false
            if (peek() is Token.Minus) { next(); neg = true }
            val t = next()
            if (t is Token.LParen) {
                var innerNeg = false
                if (peek() is Token.Minus) { next(); innerNeg = true }
                val num = next()
                if (num !is Token.Number) throw IllegalArgumentException("expected exponent, found: $num")
                val rp = next()
                if (rp !is Token.RParen) throw IllegalArgumentException("expected ')', found: $rp")
                return checkedExp(if (neg != innerNeg) -num.value else num.value, num.text)
            }
            if (t !is Token.Number) throw IllegalArgumentException("expected exponent, found: $t")
            return checkedExp(if (neg) -t.value else t.value, t.text)
        }

        private fun checkedExp(v: Double, text: String): Int {
            if (v % 1.0 != 0.0) throw IllegalArgumentException("non-integer exponent: $text")
            return v.toInt()
        }

        private fun parseUnary(): Quantity {
            if (peek() is Token.Minus) {
                next()
                val q = parseUnary()
                return Quantity(-q.factor, q.dim)
            }
            return parsePrimary()
        }

        private fun parsePrimary(): Quantity {
            return when (val t = next()) {
                is Token.Name -> {
                    val u = resolve(t.text)
                    Quantity(u.factor, u.dim)
                }
                is Token.Number -> Quantity(t.value, IntArray(7))
                is Token.LParen -> {
                    val q = parseExpr()
                    val rp = next()
                    if (rp !is Token.RParen) throw IllegalArgumentException("expected ')', found: $rp")
                    q
                }
                else -> throw IllegalArgumentException("expected unit or '(', found: $t")
            }
        }
    }

    private fun describe(dim: IntArray): String {
        if (dim.contentEquals(d())) return "dimensionless"
        if (dim.contentEquals(d(l = 1))) return "length"
        if (dim.contentEquals(d(m = 1))) return "mass"
        if (dim.contentEquals(d(t = 1))) return "time"
        if (dim.contentEquals(d(i = 1))) return "current"
        if (dim.contentEquals(d(th = 1))) return "temperature"
        if (dim.contentEquals(d(n = 1))) return "amount"
        if (dim.contentEquals(d(j = 1))) return "luminous intensity"
        if (dim.contentEquals(d(l = 1, m = 1, t = -2))) return "force"
        if (dim.contentEquals(d(l = 2, m = 1, t = -2))) return "energy"
        if (dim.contentEquals(d(l = 2, m = 1, t = -3))) return "power"
        if (dim.contentEquals(d(l = -1, m = 1, t = -2))) return "pressure"
        if (dim.contentEquals(d(l = 1, t = -1))) return "velocity"
        if (dim.contentEquals(d(l = 1, t = -2))) return "acceleration"
        if (dim.contentEquals(d(l = 2))) return "area"
        if (dim.contentEquals(d(l = 3))) return "volume"
        if (dim.contentEquals(d(t = 1, i = 1))) return "charge"
        if (dim.contentEquals(d(l = 2, m = 1, t = -3, i = -1))) return "voltage"
        if (dim.contentEquals(d(l = 2, m = 1, t = -3, i = -2))) return "resistance"
        return "dimension [${dim.joinToString(", ")}]"
    }

    fun evaluate(expr: String): Quantity {
        val t = expr.trim()
        if (t.isEmpty() || t == "1" || t.equals("unitless", ignoreCase = true) || t.equals("dimensionless", ignoreCase = true)) {
            return Quantity(1.0, IntArray(7))
        }
        return Parser(tokenize(normalize(t))).parse()
    }

    fun convertExpr(value: Double, from: String, to: String): Double {
        val q1 = evaluate(from)
        val q2 = evaluate(to)
        if (!q1.dim.contentEquals(q2.dim)) {
            throw IllegalArgumentException("incompatible: ${describe(q1.dim)} vs ${describe(q2.dim)}")
        }
        return value * q1.factor / q2.factor
    }
}
