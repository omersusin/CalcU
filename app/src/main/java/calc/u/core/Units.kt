package calc.u.core

// Conversion factors retyped from unitconverterultimate (Apache-2.0).

object Units {
    data class UnitDef(val id: String, val toBase: Double, val offset: Double = 0.0)

    val length = mapOf(
        "mm" to UnitDef("mm", 0.001), "cm" to UnitDef("cm", 0.01),
        "m" to UnitDef("m", 1.0), "km" to UnitDef("km", 1000.0),
        "in" to UnitDef("in", 0.0254), "ft" to UnitDef("ft", 0.3048),
        "yd" to UnitDef("yd", 0.9144), "mi" to UnitDef("mi", 1609.344)
    )
    val mass = mapOf(
        "mg" to UnitDef("mg", 1e-6), "g" to UnitDef("g", 0.001),
        "kg" to UnitDef("kg", 1.0), "t" to UnitDef("t", 1000.0),
        "oz" to UnitDef("oz", 0.028349523125), "lb" to UnitDef("lb", 0.45359237)
    )
    val volume = mapOf(
        "mL" to UnitDef("mL", 0.001), "L" to UnitDef("L", 1.0),
        "m3" to UnitDef("m3", 1000.0), "tsp" to UnitDef("tsp", 0.00492892),
        "tbsp" to UnitDef("tbsp", 0.0147868), "cup" to UnitDef("cup", 0.24),
        "fl-oz" to UnitDef("fl-oz", 0.0295735), "gal" to UnitDef("gal", 3.78541)
    )
    val temperature = listOf("C", "F", "K")
    val area = mapOf(
        "m2" to UnitDef("m2", 1.0), "km2" to UnitDef("km2", 1e6),
        "ft2" to UnitDef("ft2", 0.092903), "acre" to UnitDef("acre", 4046.86),
        "ha" to UnitDef("ha", 10000.0)
    )
    val speed = mapOf(
        "m/s" to UnitDef("m/s", 1.0), "km/h" to UnitDef("km/h", 1.0 / 3.6),
        "mph" to UnitDef("mph", 0.44704), "ft/s" to UnitDef("ft/s", 0.3048)
    )
    val pressure = mapOf(
        "Pa" to UnitDef("Pa", 1.0), "kPa" to UnitDef("kPa", 1000.0),
        "bar" to UnitDef("bar", 1e5), "psi" to UnitDef("psi", 6894.76),
        "atm" to UnitDef("atm", 101325.0), "mmHg" to UnitDef("mmHg", 133.322)
    )
    val energy = mapOf(
        "J" to UnitDef("J", 1.0), "kJ" to UnitDef("kJ", 1000.0),
        "cal" to UnitDef("cal", 4.184), "kcal" to UnitDef("kcal", 4184.0),
        "kWh" to UnitDef("kWh", 3.6e6)
    )
    val power = mapOf(
        "W" to UnitDef("W", 1.0), "kW" to UnitDef("kW", 1000.0),
        "hp" to UnitDef("hp", 745.7)
    )
    val data = mapOf(
        "B" to UnitDef("B", 1.0), "KB" to UnitDef("KB", 1024.0),
        "MB" to UnitDef("MB", 1024.0 * 1024), "GB" to UnitDef("GB", 1024.0 * 1024 * 1024),
        "TB" to UnitDef("TB", 1024.0 * 1024 * 1024 * 1024)
    )
    val fuel = mapOf(
        "l_100km" to UnitDef("l_100km", 1.0),
        "L/100km" to UnitDef("L/100km", 1.0),
        "mpg_us" to UnitDef("mpg_us", Double.NaN),
        "mpg" to UnitDef("mpg", Double.NaN),
        "km_l" to UnitDef("km_l", Double.NaN),
        "km/L" to UnitDef("km/L", Double.NaN)
    )

    private val fuelIds = fuel.keys
    private const val MPG_US_CONST = 235.214583

    fun fuelToL100km(v: Double, from: String): Double = when (from) {
        "mpg_us", "mpg" -> MPG_US_CONST / v
        "km_l", "km/L" -> 100.0 / v
        else -> v
    }

    fun l100kmToFuel(v: Double, to: String): Double = when (to) {
        "mpg_us", "mpg" -> MPG_US_CONST / v
        "km_l", "km/L" -> 100.0 / v
        else -> v
    }

    fun convertFuel(value: Double, from: String, to: String): Double {
        require(from in fuelIds) { "unknown fuel unit: $from" }
        require(to in fuelIds) { "unknown fuel unit: $to" }
        return l100kmToFuel(fuelToL100km(value, from), to)
    }

    fun convert(value: Double, from: UnitDef, to: UnitDef): Double {
        val fromFuel = from.id in fuelIds
        val toFuel = to.id in fuelIds
        if (fromFuel || toFuel) {
            require(fromFuel && toFuel) { "cannot mix fuel and linear units" }
            return convertFuel(value, from.id, to.id)
        }
        return value * from.toBase / to.toBase
    }

    fun convertTemp(v: Double, from: String, to: String): Double {
        val c = when (from) { "C" -> v; "F" -> (v - 32) * 5 / 9; "K" -> v - 273.15; else -> v }
        return when (to) { "C" -> c; "F" -> c * 9 / 5 + 32; "K" -> c + 273.15; else -> c }
    }

    fun ftInToCm(ft: Double, inch: Double): Double = ft * 30.48 + inch * 2.54

    fun toRoman(n: Int): String {
        if (n !in 1..3999) return "—"
        val table = listOf(1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC", 50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
        var x = n
        val sb = StringBuilder()
        for ((v, s) in table) while (x >= v) { sb.append(s); x -= v }
        return sb.toString()
    }

    fun fromBase(value: Double, base: Int): String {
        require(base in 2..36) { "base must be in 2..36" }
        if (!value.isFinite()) return "Error"
        if (value == 0.0) return "0"
        val digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val neg = value < 0
        var rest = kotlin.math.abs(value)
        var intPart = kotlin.math.floor(rest).toLong()
        var frac = rest - intPart
        val sb = StringBuilder()
        if (intPart == 0L) sb.append('0')
        val intSb = StringBuilder()
        while (intPart > 0) {
            intSb.append(digits[(intPart % base).toInt()])
            intPart /= base
        }
        if (intSb.isNotEmpty()) sb.clear().append(intSb.reverse())
        if (frac > 0.0) {
            sb.append('.')
            var count = 0
            while (frac > 0.0 && count < 10) {
                frac *= base
                val d = kotlin.math.floor(frac).toInt()
                sb.append(digits[d])
                frac -= d
                count++
            }
            var end = sb.length
            while (end > 0 && sb[end - 1] == '0') end--
            if (end > 0 && sb[end - 1] == '.') end--
            sb.setLength(end)
        }
        if (neg) sb.insert(0, '-')
        return sb.toString()
    }
}
