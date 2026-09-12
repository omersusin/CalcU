package calc.u.core

// Conversion factors retyped from unitconverterultimate (Apache-2.0).
// Coverage cross-checked against ConvertAll's unit list (GPL); all values
// below were retyped from standard definitions, not copied from its data files.

object Units {
    data class UnitDef(val id: String, val toBase: Double, val offset: Double = 0.0)

    val length = mapOf(
        "mm" to UnitDef("mm", 0.001), "cm" to UnitDef("cm", 0.01),
        "m" to UnitDef("m", 1.0), "km" to UnitDef("km", 1000.0),
        "in" to UnitDef("in", 0.0254), "ft" to UnitDef("ft", 0.3048),
        "yd" to UnitDef("yd", 0.9144), "mi" to UnitDef("mi", 1609.344),
        "Å" to UnitDef("Å", 1e-10), "ly" to UnitDef("ly", 9.4607304725808e15),
        "pc" to UnitDef("pc", 3.08567758149137e16), "nmi" to UnitDef("nmi", 1852.0),
        "mil" to UnitDef("mil", 2.54e-5), "hand" to UnitDef("hand", 0.1016),
        "link" to UnitDef("link", 0.201168), "chain" to UnitDef("chain", 20.1168),
        "rod" to UnitDef("rod", 5.0292), "fathom" to UnitDef("fathom", 1.8288)
    )
    val mass = mapOf(
        "mg" to UnitDef("mg", 1e-6), "g" to UnitDef("g", 0.001),
        "kg" to UnitDef("kg", 1.0), "t" to UnitDef("t", 1000.0),
        "oz" to UnitDef("oz", 0.028349523125), "lb" to UnitDef("lb", 0.45359237),
        "gr" to UnitDef("gr", 6.479891e-5), "st" to UnitDef("st", 6.35029318),
        "slug" to UnitDef("slug", 14.59390294)
    )
    val volume = mapOf(
        "mL" to UnitDef("mL", 0.001), "L" to UnitDef("L", 1.0),
        "m3" to UnitDef("m3", 1000.0), "tsp" to UnitDef("tsp", 0.00492892),
        "tbsp" to UnitDef("tbsp", 0.0147868), "cup" to UnitDef("cup", 0.24),
        "fl-oz" to UnitDef("fl-oz", 0.0295735), "gal" to UnitDef("gal", 3.78541),
        "qt" to UnitDef("qt", 0.946352946), "pt" to UnitDef("pt", 0.473176473),
        "fl_dram" to UnitDef("fl_dram", 0.0036966875), "tbsp_metric" to UnitDef("tbsp_metric", 0.015)
    )
    val temperature = listOf("C", "F", "K")
    val area = mapOf(
        "m2" to UnitDef("m2", 1.0), "km2" to UnitDef("km2", 1e6),
        "ft2" to UnitDef("ft2", 0.092903), "acre" to UnitDef("acre", 4046.86),
        "ha" to UnitDef("ha", 10000.0), "rood" to UnitDef("rood", 1011.715),
        "section" to UnitDef("section", 2589988.110336), "township" to UnitDef("township", 93239571.972096)
    )
    val time = mapOf(
        "s" to UnitDef("s", 1.0), "min" to UnitDef("min", 60.0),
        "hr" to UnitDef("hr", 3600.0), "day" to UnitDef("day", 86400.0),
        "week" to UnitDef("week", 604800.0), "fortnight" to UnitDef("fortnight", 1209600.0),
        "yr" to UnitDef("yr", 31556925.9746784)
    )
    val printing = mapOf(
        "point" to UnitDef("point", 0.3527777777777778),
        "pica" to UnitDef("pica", 4.233333333333333)
    )
    val speed = mapOf(
        "m/s" to UnitDef("m/s", 1.0), "km/h" to UnitDef("km/h", 1.0 / 3.6),
        "mph" to UnitDef("mph", 0.44704), "ft/s" to UnitDef("ft/s", 0.3048),
        "knot" to UnitDef("knot", 0.5144444444444445)
    )
    val pressure = mapOf(
        "Pa" to UnitDef("Pa", 1.0), "kPa" to UnitDef("kPa", 1000.0),
        "bar" to UnitDef("bar", 1e5), "psi" to UnitDef("psi", 6894.76),
        "atm" to UnitDef("atm", 101325.0), "mmHg" to UnitDef("mmHg", 133.322),
        "inHg" to UnitDef("inHg", 3386.389), "torr" to UnitDef("torr", 133.322368),
        "ksi" to UnitDef("ksi", 6894760.0)
    )
    val energy = mapOf(
        "J" to UnitDef("J", 1.0), "kJ" to UnitDef("kJ", 1000.0),
        "cal" to UnitDef("cal", 4.184), "kcal" to UnitDef("kcal", 4184.0),
        "kWh" to UnitDef("kWh", 3.6e6), "Btu" to UnitDef("Btu", 1055.05585262),
        "therm" to UnitDef("therm", 105505585.262), "tonTNT" to UnitDef("tonTNT", 4.184e9)
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
    val cooking = mapOf(
        "tsp" to UnitDef("tsp", 4.92892159375),
        "tbsp" to UnitDef("tbsp", 14.78676478125),
        "fl_oz" to UnitDef("fl_oz", 29.5735295625),
        "cup" to UnitDef("cup", 236.5882365),
        "pint_us" to UnitDef("pint_us", 473.176473),
        "pint_uk" to UnitDef("pint_uk", 568.26125),
        "quart_us" to UnitDef("quart_us", 946.352946),
        "quart_uk" to UnitDef("quart_uk", 1136.5225),
        "gallon_us" to UnitDef("gallon_us", 3785.411784),
        "gallon_uk" to UnitDef("gallon_uk", 4546.09),
        "ml" to UnitDef("ml", 1.0),
        "l" to UnitDef("l", 1000.0)
    )
    val shoe = mapOf(
        "US_M" to UnitDef("US_M", 3.0),
        "US_W" to UnitDef("US_W", 2.5714285714285716),
        "UK" to UnitDef("UK", 3.375),
        "EU" to UnitDef("EU", 0.6428571428571429),
        "CM" to UnitDef("CM", 1.0)
    )
    val ring = mapOf(
        "US" to UnitDef("US", 8.666666666666666),
        "UK" to UnitDef("UK", 9.454545454545455),
        "EU" to UnitDef("EU", 1.0),
        "JP" to UnitDef("JP", 4.333333333333333)
    )
    val historic = mapOf(
        "cubit" to UnitDef("cubit", 0.4572),
        "furlong" to UnitDef("furlong", 201.168),
        "light_year" to UnitDef("light_year", 9.4607304725808E15),
        "angstrom" to UnitDef("angstrom", 1.0E-10),
        "parsec" to UnitDef("parsec", 3.08567758149137E16),
        "stone" to UnitDef("stone", 6.35029318),
        "grain" to UnitDef("grain", 6.479891E-5)
    )
    val angle = mapOf(
        "deg" to UnitDef("deg", 0.017453292519943295),
        "rad" to UnitDef("rad", 1.0),
        "grad" to UnitDef("grad", 0.015707963267948967),
        "arcmin" to UnitDef("arcmin", 2.908882086657216E-4),
        "arcsec" to UnitDef("arcsec", 4.84813681109536E-6),
        "turn" to UnitDef("turn", 6.283185307179586)
    )
    val force = mapOf(
        "N" to UnitDef("N", 1.0), "kN" to UnitDef("kN", 1000.0),
        "lbf" to UnitDef("lbf", 4.4482216152605), "dyn" to UnitDef("dyn", 1.0E-5),
        "kgf" to UnitDef("kgf", 9.80665)
    )
    val torque = mapOf(
        "N·m" to UnitDef("N·m", 1.0), "lbf·ft" to UnitDef("lbf·ft", 1.3558179483314004),
        "lbf·in" to UnitDef("lbf·in", 0.1129848290276167), "kgf·m" to UnitDef("kgf·m", 9.80665)
    )
    val acceleration = mapOf(
        "m/s²" to UnitDef("m/s²", 1.0), "g" to UnitDef("g", 9.80665),
        "ft/s²" to UnitDef("ft/s²", 0.3048), "Gal" to UnitDef("Gal", 0.01)
    )
    val flow = mapOf(
        "L/s" to UnitDef("L/s", 0.001), "L/min" to UnitDef("L/min", 1.6666666666666667E-5),
        "m³/s" to UnitDef("m³/s", 1.0), "m³/h" to UnitDef("m³/h", 2.7777777777777776E-4),
        "gpm" to UnitDef("gpm", 6.30901964E-5), "cfm" to UnitDef("cfm", 4.719474432E-4)
    )
    val datarate = mapOf(
        "bps" to UnitDef("bps", 1.0), "kbps" to UnitDef("kbps", 1000.0),
        "Mbps" to UnitDef("Mbps", 1000000.0), "Gbps" to UnitDef("Gbps", 1000000000.0),
        "Bps" to UnitDef("Bps", 8.0), "KBps" to UnitDef("KBps", 8000.0),
        "MBps" to UnitDef("MBps", 8000000.0)
    )
    val viscosity = mapOf(
        "Pa·s" to UnitDef("Pa·s", 1.0), "mPa·s" to UnitDef("mPa·s", 0.001),
        "cP" to UnitDef("cP", 0.001), "P" to UnitDef("P", 0.1),
        "lb/(ft·s)" to UnitDef("lb/(ft·s)", 1.4881639),
        "lbf·s/ft²" to UnitDef("lbf·s/ft²", 47.880258)
    )
    // Gy and Sv share the J/kg dimension here; biological weighting is not modeled.
    val radiation = mapOf(
        "Sv" to UnitDef("Sv", 1.0), "mSv" to UnitDef("mSv", 0.001),
        "uSv" to UnitDef("uSv", 1e-6), "rem" to UnitDef("rem", 0.01),
        "mrem" to UnitDef("mrem", 1e-5), "Gy" to UnitDef("Gy", 1.0),
        "mGy" to UnitDef("mGy", 0.001), "rad" to UnitDef("rad", 0.01)
    )
    val illuminance = mapOf(
        "lux" to UnitDef("lux", 1.0), "mlx" to UnitDef("mlx", 0.001),
        "klx" to UnitDef("klx", 1000.0), "fc" to UnitDef("fc", 10.76391041671),
        "ph" to UnitDef("ph", 10000.0)
    )
    val magnetic = mapOf(
        "T" to UnitDef("T", 1.0), "mT" to UnitDef("mT", 0.001),
        "uT" to UnitDef("uT", 1e-6), "G" to UnitDef("G", 1e-4),
        "kG" to UnitDef("kG", 0.1), "mG" to UnitDef("mG", 1e-7)
    )
    val density = mapOf(
        "kg/m³" to UnitDef("kg/m³", 1.0), "g/cm³" to UnitDef("g/cm³", 1000.0),
        "g/mL" to UnitDef("g/mL", 1000.0), "kg/L" to UnitDef("kg/L", 1000.0),
        "g/L" to UnitDef("g/L", 1.0), "lb/ft³" to UnitDef("lb/ft³", 16.01846337395),
        "lb/in³" to UnitDef("lb/in³", 27679.90471)
    )
    val specificenergy = mapOf(
        "J/kg" to UnitDef("J/kg", 1.0), "kJ/kg" to UnitDef("kJ/kg", 1000.0),
        "Wh/kg" to UnitDef("Wh/kg", 3600.0), "cal/g" to UnitDef("cal/g", 4184.0),
        "kcal/kg" to UnitDef("kcal/kg", 4184.0), "BTU/lb" to UnitDef("BTU/lb", 2326.0)
    )

    // Sound level (dB) intentionally absent: decibels are logarithmic and need a
    // reference quantity, so they cannot use the linear toBase model above.

    private val fuelIds = fuel.keys
    private const val MPG_US_CONST = 235.214583

    fun fuelToL100km(v: Double, from: String): Double = when (from) {
        "mpg_us", "mpg" -> {
            require(v != 0.0) { "fuel economy must be non-zero" }
            MPG_US_CONST / v
        }
        "km_l", "km/L" -> {
            require(v != 0.0) { "fuel economy must be non-zero" }
            100.0 / v
        }
        else -> v
    }

    fun l100kmToFuel(v: Double, to: String): Double = when (to) {
        "mpg_us", "mpg" -> {
            require(v != 0.0) { "fuel economy must be non-zero" }
            MPG_US_CONST / v
        }
        "km_l", "km/L" -> {
            require(v != 0.0) { "fuel economy must be non-zero" }
            100.0 / v
        }
        else -> v
    }

    fun convertFuel(value: Double, from: String, to: String): Double {
        require(from in fuelIds) { "unknown fuel unit: $from" }
        require(to in fuelIds) { "unknown fuel unit: $to" }
        return l100kmToFuel(fuelToL100km(value, from), to)
    }

    fun convertCookingToWeight(volumeMl: Double, gramsPerCup: Double): Double {
        val cupMl = cooking["cup"]?.toBase ?: 236.5882365
        return volumeMl * gramsPerCup / cupMl
    }

    fun convert(value: Double, from: UnitDef, to: UnitDef): Double {
        val fromFuel = from.id in fuelIds
        val toFuel = to.id in fuelIds
        if (fromFuel || toFuel) {
            require(fromFuel && toFuel) { "cannot mix fuel and linear units" }
            return convertFuel(value, from.id, to.id)
        }
        require(from.toBase.isFinite()) { "source factor must be finite" }
        require(to.toBase.isFinite()) { "target factor must be finite" }
        require(to.toBase != 0.0) { "target factor must be non-zero" }
        return value * from.toBase / to.toBase
    }

    fun convertTemp(v: Double, from: String, to: String): Double {
        require(from == "C" || from == "F" || from == "K") { "unknown temperature unit: $from" }
        require(to == "C" || to == "F" || to == "K") { "unknown temperature unit: $to" }
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

    // Magnitude-aware display without grouping (locale-independent, no commas or
    // spaces) so output stays parseable across phone locales. Six significant
    // digits via %g; callers needing fixed decimals should keep formatting manually.
    fun fmtMag(v: Double): String {
        if (!v.isFinite()) return "—"
        if (v == 0.0) return "0"
        return try {
            String.format(java.util.Locale.US, "%.6g", v)
        } catch (_: Exception) {
            "—"
        }
    }
}
