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
        "rod" to UnitDef("rod", 5.0292), "fathom" to UnitDef("fathom", 1.8288),
        "dm" to UnitDef("dm", 0.1), "um" to UnitDef("um", 1e-6),
        "nm" to UnitDef("nm", 1e-9), "dam" to UnitDef("dam", 10.0),
        "hm" to UnitDef("hm", 100.0), "Mm" to UnitDef("Mm", 1e6),
        "fermi" to UnitDef("fermi", 1e-15), "bohr" to UnitDef("bohr", 5.29177210903e-11),
        "au" to UnitDef("au", 1.495978707e11), "LD" to UnitDef("LD", 3.844e8),
        "lightmin" to UnitDef("lightmin", 1.798754748e10), "lightsec" to UnitDef("lightsec", 2.99792458e8),
        "kpc" to UnitDef("kpc", 3.08567758149137e19), "Mpc" to UnitDef("Mpc", 3.08567758149137e22),
        "league" to UnitDef("league", 4828.032), "cable" to UnitDef("cable", 185.2),
        "smoot" to UnitDef("smoot", 1.7018), "versta" to UnitDef("versta", 1066.8),
        "marathon" to UnitDef("marathon", 42194.988),
        "pm" to UnitDef("pm", 1e-12), // picometer, exact SI definition,
        "surveyFoot" to UnitDef("surveyFoot", 1200.0 / 3937.0), // US survey foot, exact definition (ConvertAll ideas-only),
        // Informal units below: handy comparisons, not for engineering use.
        "footballField" to UnitDef("footballField", 91.44), // informal: 100 yd (ConvertAll ideas-only),
        "rackUnit" to UnitDef("rackUnit", 0.04445), // informal: 1.75 in computing (ConvertAll ideas-only),
        "swedishMil" to UnitDef("swedishMil", 10000.0) // informal: 10 km (ConvertAll ideas-only),
    )
    val mass = mapOf(
        "mg" to UnitDef("mg", 1e-6), "g" to UnitDef("g", 0.001),
        "kg" to UnitDef("kg", 1.0), "t" to UnitDef("t", 1000.0),
        "oz" to UnitDef("oz", 0.028349523125), "lb" to UnitDef("lb", 0.45359237),
        "gr" to UnitDef("gr", 6.479891e-5), "st" to UnitDef("st", 6.35029318),
        "slug" to UnitDef("slug", 14.59390294),
        "hg" to UnitDef("hg", 0.1), "dag" to UnitDef("dag", 0.01),
        "dg" to UnitDef("dg", 1e-4), "cg" to UnitDef("cg", 1e-5),
        "ug" to UnitDef("ug", 1e-9), "kilotonne" to UnitDef("kilotonne", 1e6),
        "carat" to UnitDef("carat", 2e-4), "amu" to UnitDef("amu", 1.660539066605e-27),
        "dram" to UnitDef("dram", 0.0017718451953125), "dwt" to UnitDef("dwt", 0.00155517384),
        "ton_us" to UnitDef("ton_us", 907.18474), "longton" to UnitDef("longton", 1016.0469088),
        // Troy mass: 1 grain = 64.79891 mg exactly; lb-t = 5760 gr, oz-t = 480 gr (ConvertAll ideas-only).
        "ozt" to UnitDef("ozt", 0.0311034768), "lbt" to UnitDef("lbt", 0.3732417216),
        "ettogram" to UnitDef("ettogram", 0.1), // 100 g Scandinavian unit, exact definition,
        "quintal" to UnitDef("quintal", 100.0), // 100 kg, exact definition,
        "cwtShort" to UnitDef("cwtShort", 45.359237), // hundredweight short = 100 lb (ConvertAll ideas-only),
        "cwtLong" to UnitDef("cwtLong", 50.80234544) // hundredweight long = 112 lb Imperial (ConvertAll ideas-only),
    )
    val volume = mapOf(
        "mL" to UnitDef("mL", 0.001), "L" to UnitDef("L", 1.0),
        "m3" to UnitDef("m3", 1000.0), "tsp" to UnitDef("tsp", 0.00492892159375),
        // cupLegal: US legal cup = 240 mL (nutrition labeling); cooking uses cupCustomary below.
        "tbsp" to UnitDef("tbsp", 0.01478676478125), "cup" to UnitDef("cup", 0.24),
        "fl-oz" to UnitDef("fl-oz", 0.0295735295625), "gal" to UnitDef("gal", 3.785411784),
        "qt" to UnitDef("qt", 0.946352946), "pt" to UnitDef("pt", 0.473176473),
        "fl_dram" to UnitDef("fl_dram", 0.0036966911953125), "tbsp_metric" to UnitDef("tbsp_metric", 0.015),
        "dL" to UnitDef("dL", 0.1), "cL" to UnitDef("cL", 0.01),
        "uL" to UnitDef("uL", 1e-6), "daL" to UnitDef("daL", 10.0),
        "hL" to UnitDef("hL", 100.0), "kL" to UnitDef("kL", 1000.0),
        "ML" to UnitDef("ML", 1e6), "minim" to UnitDef("minim", 6.1611519921875e-5),
        "barrel" to UnitDef("barrel", 158.987294928), "shot" to UnitDef("shot", 0.04436029434375),
        "fifth" to UnitDef("fifth", 0.7570823568), "winebottle" to UnitDef("winebottle", 0.75),
        "magnum" to UnitDef("magnum", 1.5), "keg" to UnitDef("keg", 58.673882579),
        "hogsheadWine" to UnitDef("hogsheadWine", 238.480942384),
        "hogsheadBeer" to UnitDef("hogsheadBeer", 204.412522056),
        "bushel" to UnitDef("bushel", 35.23907016688), "peck" to UnitDef("peck", 8.80976754172),
        "cord" to UnitDef("cord", 3624.556363776), "boardfoot" to UnitDef("boardfoot", 2.359737216),
        "impMinim" to UnitDef("impMinim", 5.919388020833333e-5),
        "tspUK" to UnitDef("tspUK", 0.00591938802083),
        "tbspUK" to UnitDef("tbspUK", 0.0177581640625),
        "flozUK" to UnitDef("flozUK", 0.0284130625),
        "ptUK" to UnitDef("ptUK", 0.56826125),
        "qtUK" to UnitDef("qtUK", 1.1365225),
        "galUK" to UnitDef("galUK", 4.54609),
        // Imperial/metric kitchen extras: cupUK is half an Imperial pint exactly (ConvertAll ideas-only).
        "cupUK" to UnitDef("cupUK", 0.284130625), "cupMetric" to UnitDef("cupMetric", 0.25),
        "tbspAU" to UnitDef("tbspAU", 0.02), // Australian tablespoon = 20 mL, exact definition,
        "tspMetric" to UnitDef("tspMetric", 0.005), // metric teaspoon = 5 mL, exact definition,
        "gillUS" to UnitDef("gillUS", 0.11829411825), // 4 US fl oz, exact definition,
        "gillUK" to UnitDef("gillUK", 0.1420653125), // 5 Imperial fl oz, exact definition,
        "barrelUK" to UnitDef("barrelUK", 163.65924), // 36 Imperial gal, exact definition,
        "in3" to UnitDef("in3", 0.016387064), // cubic inch, exact definition,
        "ft3" to UnitDef("ft3", 28.316846592), // cubic foot, exact definition,
        "yd3" to UnitDef("yd3", 764.554857984) // cubic yard, exact definition,
    )
    /**
     * Temperature ids for [convertTemp]. C/F/K/R are the original four; Reaumur,
     * Romer, Delisle and Newton are affine scales handled the same way (exact
     * formulas retyped from unitconverterultimate TemperatureConverter,
     * Apache-2.0). Gas mark is NOT listed here: it is a non-linear oven table,
     * see [gasMarkToC] and [cToGasMark].
     */
    val temperature = listOf("C", "F", "K", "R", "Reaumur", "Romer", "Delisle", "Newton")
    val area = mapOf(
        "m2" to UnitDef("m2", 1.0), "km2" to UnitDef("km2", 1e6),
        "ft2" to UnitDef("ft2", 0.09290304), "acre" to UnitDef("acre", 4046.8564224),
        "ha" to UnitDef("ha", 10000.0), "rood" to UnitDef("rood", 1011.7141056),
        "section" to UnitDef("section", 2589988.110336), "township" to UnitDef("township", 93239571.972096),
        "barn" to UnitDef("barn", 1e-28), "are" to UnitDef("are", 100.0),
        "decare" to UnitDef("decare", 1000.0), "stremma" to UnitDef("stremma", 1000.0),
        "homestead" to UnitDef("homestead", 647497.02784), "sqperch" to UnitDef("sqperch", 25.29285264),
        "rai" to UnitDef("rai", 1600.0), "circInch" to UnitDef("circInch", 5.067074790974978e-4),
        "circMil" to UnitDef("circMil", 5.067074790974978e-10),
        "mm2" to UnitDef("mm2", 1e-6), "cm2" to UnitDef("cm2", 1e-4),
        "in2" to UnitDef("in2", 6.4516e-4), "yd2" to UnitDef("yd2", 0.83612736),
        "mi2" to UnitDef("mi2", 2589988.110336),
        "dm2" to UnitDef("dm2", 0.01), // square decimeter, exact definition,
        "um2" to UnitDef("um2", 1e-12) // square micrometer, exact definition,
    )
    val time = mapOf(
        "s" to UnitDef("s", 1.0), "min" to UnitDef("min", 60.0),
        "hr" to UnitDef("hr", 3600.0), "day" to UnitDef("day", 86400.0),
        "week" to UnitDef("week", 604800.0), "fortnight" to UnitDef("fortnight", 1209600.0),
        "yr" to UnitDef("yr", 31556925.9746784), // tropical year 365.242198781 d (not Julian 365.25 d),
        "ms" to UnitDef("ms", 0.001), "us" to UnitDef("us", 1e-6),
        "ns" to UnitDef("ns", 1e-9), "month" to UnitDef("month", 2629743.8312232),
        "decade" to UnitDef("decade", 315569259.746784), "century" to UnitDef("century", 3.15569259746784e9),
        "millennium" to UnitDef("millennium", 3.15569259746784e10),
        "ds" to UnitDef("ds", 0.1), // decisecond, exact definition,
        "cs" to UnitDef("cs", 0.01) // centisecond, exact definition,
    )
    val printing = mapOf(
        "point" to UnitDef("point", 0.3527777777777778),
        "pica" to UnitDef("pica", 4.233333333333333)
    )
    val speed = mapOf(
        "m/s" to UnitDef("m/s", 1.0), "km/h" to UnitDef("km/h", 1.0 / 3.6),
        "mph" to UnitDef("mph", 0.44704), "ft/s" to UnitDef("ft/s", 0.3048),
        "knot" to UnitDef("knot", 0.5144444444444445),
        "lightspeed" to UnitDef("lightspeed", 2.99792458e8), "mach" to UnitDef("mach", 340.29),
        "km/s" to UnitDef("km/s", KM_S_TO_BASE), "kms" to UnitDef("kms", KM_S_TO_BASE) // kms is an alias of km/s; single shared factor,
    )
    val pressure = mapOf(
        "Pa" to UnitDef("Pa", 1.0), "kPa" to UnitDef("kPa", 1000.0),
        "bar" to UnitDef("bar", 1e5), "psi" to UnitDef("psi", 6894.757293168),
        "atm" to UnitDef("atm", 101325.0), "mmHg" to UnitDef("mmHg", 133.322387415),
        "inHg" to UnitDef("inHg", 3386.389), "torr" to UnitDef("torr", 133.322368), // torr kept: ~0.14 ppm below mmHg by convention,
        "ksi" to UnitDef("ksi", 6894757.293168),
        "hPa" to UnitDef("hPa", 100.0), "MPa" to UnitDef("MPa", 1e6),
        "GPa" to UnitDef("GPa", 1e9), "mbar" to UnitDef("mbar", 100.0),
        "microbar" to UnitDef("microbar", 0.1), "decibar" to UnitDef("decibar", 10000.0),
        "kilobar" to UnitDef("kilobar", 1e8), "megabar" to UnitDef("megabar", 1e11),
        // mmHg from Hg density 13.5951 g/cm3 (NOT 101325/760 like torr); keep both, do not merge.
        "mmWater" to UnitDef("mmWater", 9.80665), "inWater" to UnitDef("inWater", 249.08891),
        "ftWater" to UnitDef("ftWater", 2989.06692),
        "kgfcm2" to UnitDef("kgfcm2", 98066.5),
        "at" to UnitDef("at", 98066.5), // technical atmosphere, alias of kgf/cm2 (not standard atm),
        "mH2O" to UnitDef("mH2O", 9806.65), // meter of water column: 1000 kg/m3 * g * 1 m,
        "psf" to UnitDef("psf", 47.8802589800556) // pound per square foot = psi / 144,
    )
    val energy = mapOf(
        "J" to UnitDef("J", 1.0), "kJ" to UnitDef("kJ", 1000.0),
        "cal" to UnitDef("cal", 4.184), "kcal" to UnitDef("kcal", 4184.0), // cal_IT: International Table calorie,
        "kWh" to UnitDef("kWh", 3.6e6), "Btu" to UnitDef("Btu", 1055.05585262),
        "therm" to UnitDef("therm", 105505585.262), "tonTNT" to UnitDef("tonTNT", 4.184e9),
        "MJ" to UnitDef("MJ", 1e6), "GJ" to UnitDef("GJ", 1e9),
        "mJ" to UnitDef("mJ", 0.001), "erg" to UnitDef("erg", 1e-7),
        "eV" to UnitDef("eV", 1.602176634e-19),
        "tonneOil" to UnitDef("tonneOil", 4.1868e10), "tonneCoal" to UnitDef("tonneCoal", 2.93076e10),
        "Wh" to UnitDef("Wh", 3600.0), "ftlb" to UnitDef("ftlb", 1.3558179483314)
    )
    val power = mapOf(
        "W" to UnitDef("W", 1.0), "kW" to UnitDef("kW", 1000.0),
        "hp" to UnitDef("hp", 745.6998715822702), // mechanical hp = 550 ft*lbf/s exactly,
        "MW" to UnitDef("MW", 1e6), "GW" to UnitDef("GW", 1e9),
        "mW" to UnitDef("mW", 0.001), "metricHp" to UnitDef("metricHp", 735.49875),
        "tonRefrig" to UnitDef("tonRefrig", 3516.852842066667), "MBH" to UnitDef("MBH", 293.0710701722222)
    )
    /**
     * Binary data ladder: legacy KB/MB/../EB are powers of 1024 (file-manager
     * convention), while the kBsi-style decimal SI ladder kBsi/MBsi/GBsi/TBsi/
     * PBsi/EBsi below is powers of 1000. EB here is binary-consistent
     * (1024^6 B = 1152921504606846976 B, the IEC twin of EiB); legacy KB=1024
     * versus kBsi=1000 is intentional, not a bug.
     */
    val data = mapOf(
        "B" to UnitDef("B", 1.0), "KB" to UnitDef("KB", 1024.0),
        "MB" to UnitDef("MB", 1024.0 * 1024), "GB" to UnitDef("GB", 1024.0 * 1024 * 1024),
        "TB" to UnitDef("TB", 1024.0 * 1024 * 1024 * 1024),
        "bit" to UnitDef("bit", 0.125), "kbit" to UnitDef("kbit", 125.0),
        "Mbit" to UnitDef("Mbit", 125000.0), "Gbit" to UnitDef("Gbit", 1.25e8),
        "Tbit" to UnitDef("Tbit", 1.25e11),
        "PB" to UnitDef("PB", 1024.0 * 1024 * 1024 * 1024 * 1024),
        "kBsi" to UnitDef("kBsi", 1000.0), "MBsi" to UnitDef("MBsi", 1e6),
        "GBsi" to UnitDef("GBsi", 1e9), "TBsi" to UnitDef("TBsi", 1e12),
        "PBsi" to UnitDef("PBsi", 1e15),
        "KiB" to UnitDef("KiB", 1024.0), "MiB" to UnitDef("MiB", 1048576.0),
        "GiB" to UnitDef("GiB", 1073741824.0), "TiB" to UnitDef("TiB", 1099511627776.0),
        "PiB" to UnitDef("PiB", 1125899906842624.0),
        "Pbit" to UnitDef("Pbit", 1.25e14), "Ebit" to UnitDef("Ebit", 1.25e17),
        "EB" to UnitDef("EB", 1152921504606846976.0), "EiB" to UnitDef("EiB", 1152921504606846976.0),
        "EBsi" to UnitDef("EBsi", 1e18),
        "nibble" to UnitDef("nibble", 0.5), // half byte, exact definition,
        "Kibit" to UnitDef("Kibit", 128.0), // binary kibibit = 1024 bits, exact definition,
        "Mibit" to UnitDef("Mibit", 131072.0), "Gibit" to UnitDef("Gibit", 134217728.0)
    )
    val fuel = mapOf(
        "l_100km" to UnitDef("l_100km", 1.0),
        "L/100km" to UnitDef("L/100km", 1.0),
        "mpg_us" to UnitDef("mpg_us", Double.NaN),
        "mpg" to UnitDef("mpg", Double.NaN),
        "km_l" to UnitDef("km_l", Double.NaN),
        "km/L" to UnitDef("km/L", Double.NaN),
        "mpg_uk" to UnitDef("mpg_uk", Double.NaN),
        "mpgUK" to UnitDef("mpgUK", Double.NaN)
    )
    val cooking = mapOf(
        "tsp" to UnitDef("tsp", 4.92892159375),
        "tbsp" to UnitDef("tbsp", 14.78676478125),
        "fl_oz" to UnitDef("fl_oz", 29.5735295625),
        "cup" to UnitDef("cup", 236.5882365), // cupCustomary: 8 fl oz customary (vs 240 mL cupLegal in volume),
        "pint_us" to UnitDef("pint_us", 473.176473),
        "pint_uk" to UnitDef("pint_uk", 568.26125),
        "quart_us" to UnitDef("quart_us", 946.352946),
        "quart_uk" to UnitDef("quart_uk", 1136.5225),
        "gallon_us" to UnitDef("gallon_us", 3785.411784),
        "gallon_uk" to UnitDef("gallon_uk", 4546.09),
        "ml" to UnitDef("ml", 1.0),
        "l" to UnitDef("l", 1000.0)
    )
    /**
     * Shoe sizes are affine (offset) scales, not proportional through-origin
     * factors: size 0 is not a zero-length foot. The toBase numbers below are
     * kept only so category ids stay stable for callers; [convert] routes every
     * shoe pair through [convertShoe] and never uses them as ratios.
     *
     * Reference foot lengths (Mondopoint cm) after the Brannock-device
     * approximation shoeUsMToCm(us) = (us + 22) * 2.54 / 3, cross-checked
     * against common retail charts (US_M 10 wears about 27.0-28.0 cm):
     * 6 to 23.71, 7 to 24.55, 8 to 25.40, 9 to 26.25, 10 to 27.09,
     * 11 to 27.94, 12 to 28.79, 13 to 29.63. Brand lasts vary about +-0.5 cm.
     */
    val shoe = mapOf(
        "US_M" to UnitDef("US_M", 3.0),
        "US_W" to UnitDef("US_W", 2.5714285714285716),
        "UK" to UnitDef("UK", 3.375),
        "EU" to UnitDef("EU", 0.6428571428571429),
        "CM" to UnitDef("CM", 1.0)
    )
    /**
     * Ring sizes are affine scales of inner circumference, not proportional
     * factors. The toBase numbers below are kept only so category ids stay
     * stable for callers; [convert] routes every ring pair through
     * [convertRing] and never uses them as ratios.
     *
     * Reference inner circumferences (EU/French size = mm) after standard
     * jewelry charts (US step about 2.55 mm circ, about 0.8128 mm dia):
     * US 5 to 49.35, 6 to 51.90, 8 to 57.00, 10 to 62.10 (19.77 mm dia),
     * 12 to 67.20. JP is EU minus 40; the numeric UK index is US minus 0.5
     * (letter scales vary by maker, so treat UK as an approximation).
     */
    val ring = mapOf(
        "US" to UnitDef("US", 8.666666666666666),
        "UK" to UnitDef("UK", 9.454545454545455),
        "EU" to UnitDef("EU", 1.0),
        "JP" to UnitDef("JP", 4.333333333333333)
    )

    fun shoeUsMToCm(us: Double): Double {
        require(us.isFinite()) { "shoe size must be finite" }
        return (us + 22.0) * 2.54 / 3.0
    }

    fun shoeUsWToCm(usw: Double): Double {
        require(usw.isFinite()) { "shoe size must be finite" }
        return shoeUsMToCm(usw - 1.5)
    }

    fun shoeUkToCm(uk: Double): Double {
        require(uk.isFinite()) { "shoe size must be finite" }
        return shoeUsMToCm(uk + 0.5)
    }

    fun shoeEuToCm(eu: Double): Double {
        require(eu.isFinite()) { "shoe size must be finite" }
        return (eu - 2.0) * 2.0 / 3.0
    }

    fun cmToShoeUsM(cm: Double): Double {
        require(cm.isFinite()) { "foot length must be finite" }
        return cm * 3.0 / 2.54 - 22.0
    }

    fun cmToShoeUsW(cm: Double): Double {
        require(cm.isFinite()) { "foot length must be finite" }
        return cmToShoeUsM(cm) + 1.5
    }

    fun cmToShoeUk(cm: Double): Double {
        require(cm.isFinite()) { "foot length must be finite" }
        return cmToShoeUsM(cm) - 0.5
    }

    fun cmToShoeEu(cm: Double): Double {
        require(cm.isFinite()) { "foot length must be finite" }
        return cm * 1.5 + 2.0
    }

    fun shoeToCm(value: Double, from: String): Double {
        require(value.isFinite()) { "shoe size must be finite" }
        return when (from) {
            "US_M" -> shoeUsMToCm(value)
            "US_W" -> shoeUsWToCm(value)
            "UK" -> shoeUkToCm(value)
            "EU" -> shoeEuToCm(value)
            "CM" -> value
            else -> throw IllegalArgumentException("unknown shoe unit: $from")
        }
    }

    fun cmToShoe(cm: Double, to: String): Double {
        require(cm.isFinite()) { "foot length must be finite" }
        return when (to) {
            "US_M" -> cmToShoeUsM(cm)
            "US_W" -> cmToShoeUsW(cm)
            "UK" -> cmToShoeUk(cm)
            "EU" -> cmToShoeEu(cm)
            "CM" -> cm
            else -> throw IllegalArgumentException("unknown shoe unit: $to")
        }
    }

    fun convertShoe(value: Double, from: String, to: String): Double {
        require(from in shoe) { "unknown shoe unit: $from" }
        require(to in shoe) { "unknown shoe unit: $to" }
        if (from == to) return value
        return cmToShoe(shoeToCm(value, from), to)
    }

    fun ringUsToEu(us: Double): Double {
        require(us.isFinite()) { "ring size must be finite" }
        return 36.6 + 2.55 * us
    }

    fun ringEuToUs(eu: Double): Double {
        require(eu.isFinite()) { "ring size must be finite" }
        return (eu - 36.6) / 2.55
    }

    fun ringJpToEu(jp: Double): Double {
        require(jp.isFinite()) { "ring size must be finite" }
        return jp + 40.0
    }

    fun ringEuToJp(eu: Double): Double {
        require(eu.isFinite()) { "ring size must be finite" }
        return eu - 40.0
    }

    fun ringUkToEu(uk: Double): Double {
        require(uk.isFinite()) { "ring size must be finite" }
        return 36.6 + 2.55 * (uk + 0.5)
    }

    fun ringEuToUk(eu: Double): Double {
        require(eu.isFinite()) { "ring size must be finite" }
        return (eu - 36.6) / 2.55 - 0.5
    }

    fun ringToEu(value: Double, from: String): Double {
        require(value.isFinite()) { "ring size must be finite" }
        return when (from) {
            "US" -> ringUsToEu(value)
            "UK" -> ringUkToEu(value)
            "EU" -> value
            "JP" -> ringJpToEu(value)
            else -> throw IllegalArgumentException("unknown ring unit: $from")
        }
    }

    fun euToRing(eu: Double, to: String): Double {
        require(eu.isFinite()) { "ring size must be finite" }
        return when (to) {
            "US" -> ringEuToUs(eu)
            "UK" -> ringEuToUk(eu)
            "EU" -> eu
            "JP" -> ringEuToJp(eu)
            else -> throw IllegalArgumentException("unknown ring unit: $to")
        }
    }

    fun convertRing(value: Double, from: String, to: String): Double {
        require(from in ring) { "unknown ring unit: $from" }
        require(to in ring) { "unknown ring unit: $to" }
        if (from == to) return value
        return euToRing(ringToEu(value, from), to)
    }

    private fun isShoeDef(u: UnitDef): Boolean {
        if (shoe.values.any { it === u }) return true
        val s = shoe[u.id] ?: return false
        if (s.toBase != u.toBase) return false
        val r = ring[u.id]
        return r == null || r.toBase != u.toBase
    }

    private fun isRingDef(u: UnitDef): Boolean {
        if (ring.values.any { it === u }) return true
        val r = ring[u.id] ?: return false
        if (r.toBase != u.toBase) return false
        val s = shoe[u.id]
        return s == null || s.toBase != u.toBase
    }

    val historic = mapOf(
        "cubit" to UnitDef("cubit", 0.4572), // 18-inch cubit variant,
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
        "turn" to UnitDef("turn", 6.283185307179586),
        "quadrant" to UnitDef("quadrant", 1.5707963267948966)
    )
    val force = mapOf(
        "N" to UnitDef("N", 1.0), "kN" to UnitDef("kN", 1000.0),
        "lbf" to UnitDef("lbf", 4.4482216152605), "dyn" to UnitDef("dyn", 1.0E-5),
        "kgf" to UnitDef("kgf", 9.80665),
        "daN" to UnitDef("daN", 10.0), "MN" to UnitDef("MN", 1e6),
        "mN" to UnitDef("mN", 0.001), "gf" to UnitDef("gf", 0.00980665),
        "kip" to UnitDef("kip", 4448.2216152605), "tf" to UnitDef("tf", 9806.65),
        "ozf" to UnitDef("ozf", 0.2780138509537812), "poundal" to UnitDef("poundal", 0.138254954376)
    )
    val torque = mapOf(
        "N·m" to UnitDef("N·m", 1.0), "lbf·ft" to UnitDef("lbf·ft", 1.3558179483314004),
        "lbf·in" to UnitDef("lbf·in", 0.1129848290276167), "kgf·m" to UnitDef("kgf·m", 9.80665),
        // ozf·in is exactly 1/16 lbf·in; poundal·m reuses the force-table poundal (ConvertAll ideas-only).
        "ozf·in" to UnitDef("ozf·in", 0.007061551814226042), "dyne·m" to UnitDef("dyne·m", 1e-5),
        "N·cm" to UnitDef("N·cm", 0.01), "N·mm" to UnitDef("N·mm", 0.001),
        "kgf·cm" to UnitDef("kgf·cm", 0.0980665), "poundal·m" to UnitDef("poundal·m", 0.138254954376)
    )
    val acceleration = mapOf(
        "m/s²" to UnitDef("m/s²", 1.0), "g" to UnitDef("g", 9.80665),
        "ft/s²" to UnitDef("ft/s²", 0.3048), "Gal" to UnitDef("Gal", 0.01)
    )
    val flow = mapOf(
        "L/s" to UnitDef("L/s", 0.001), "L/min" to UnitDef("L/min", 1.6666666666666667E-5),
        "m³/s" to UnitDef("m³/s", 1.0), "m³/h" to UnitDef("m³/h", 2.7777777777777776E-4),
        "gpm" to UnitDef("gpm", 6.30901964E-5), "cfm" to UnitDef("cfm", 4.719474432E-4),
        "gph" to UnitDef("gph", 1.0515032733333334e-6), "mgd" to UnitDef("mgd", 0.043812636388888895),
        "cfs" to UnitDef("cfs", 0.028316846592), "mld" to UnitDef("mld", 0.011574074074074073)
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
        "mGy" to UnitDef("mGy", 0.001), "rad" to UnitDef("rad", 0.01),
        "cGy" to UnitDef("cGy", 0.01)
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
        "lb/in³" to UnitDef("lb/in³", 27679.90471),
        "mg/L" to UnitDef("mg/L", 0.001), // clinical ladder, exact SI,
        "mg/dL" to UnitDef("mg/dL", 0.01), "g/dL" to UnitDef("g/dL", 10.0)
    )
    val specificenergy = mapOf(
        "J/kg" to UnitDef("J/kg", 1.0), "kJ/kg" to UnitDef("kJ/kg", 1000.0),
        "Wh/kg" to UnitDef("Wh/kg", 3600.0), "cal/g" to UnitDef("cal/g", 4184.0),
        "kcal/kg" to UnitDef("kcal/kg", 4184.0), "BTU/lb" to UnitDef("BTU/lb", 2326.0)
    )
    val current = mapOf(
        "A" to UnitDef("A", 1.0), "mA" to UnitDef("mA", 0.001),
        "uA" to UnitDef("uA", 1e-6), "kA" to UnitDef("kA", 1000.0)
    )
    val charge = mapOf(
        "C" to UnitDef("C", 1.0), "e" to UnitDef("e", 1.602176634e-19),
        "Ah" to UnitDef("Ah", 3600.0), "mAh" to UnitDef("mAh", 3.6)
    )
    val potential = mapOf(
        "V" to UnitDef("V", 1.0), "mV" to UnitDef("mV", 0.001),
        "kV" to UnitDef("kV", 1000.0)
    )
    val resistance = mapOf(
        "ohm" to UnitDef("ohm", 1.0), "milliohm" to UnitDef("milliohm", 0.001),
        "microhm" to UnitDef("microhm", 1e-6), "kilohm" to UnitDef("kilohm", 1000.0)
    )
    val capacitance = mapOf(
        "F" to UnitDef("F", 1.0), "mF" to UnitDef("mF", 0.001),
        "uF" to UnitDef("uF", 1e-6), "nF" to UnitDef("nF", 1e-9),
        "pF" to UnitDef("pF", 1e-12),
        // Extended SI ladder, exact definitions; statfarad via c (ConvertAll ideas-only).
        "aF" to UnitDef("aF", 1e-18), "fF" to UnitDef("fF", 1e-15),
        "kF" to UnitDef("kF", 1000.0), "MF" to UnitDef("MF", 1e6),
        "GF" to UnitDef("GF", 1e9), "statF" to UnitDef("statF", 1.112650056e-12)
    )
    val inductance = mapOf(
        "H" to UnitDef("H", 1.0), "mH" to UnitDef("mH", 0.001),
        "uH" to UnitDef("uH", 1e-6)
    )
    val solidangle = mapOf(
        "sr" to UnitDef("sr", 1.0), "sphere" to UnitDef("sphere", 12.566370614359172),
        "hemisphere" to UnitDef("hemisphere", 6.283185307179586) // half sphere = 2 pi sr (ConvertAll ideas-only),
    )
    val quantity = mapOf(
        "count" to UnitDef("count", 1.0), "dozen" to UnitDef("dozen", 12.0),
        "gross" to UnitDef("gross", 144.0), "percent" to UnitDef("percent", 0.01),
        // Everyday counts (ConvertAll ideas-only); karat is gold purity 1/24.
        "bakersDozen" to UnitDef("bakersDozen", 13.0), "score" to UnitDef("score", 20.0),
        "ream" to UnitDef("ream", 500.0), "permill" to UnitDef("permill", 0.001),
        "karat" to UnitDef("karat", 1.0 / 24.0)
    )
    val concentration = mapOf(
        "fraction" to UnitDef("fraction", 1.0), "ppm" to UnitDef("ppm", 1e-6),
        "ppb" to UnitDef("ppb", 1e-9), "ppt" to UnitDef("ppt", 1e-12) // parts per trillion (ConvertAll ideas-only),
    )
    val frequency = mapOf(
        "Hz" to UnitDef("Hz", 1.0), "kHz" to UnitDef("kHz", 1000.0),
        "MHz" to UnitDef("MHz", 1e6), "GHz" to UnitDef("GHz", 1e9),
        "mHz" to UnitDef("mHz", 0.001) // millihertz, exact SI (ConvertAll ideas-only),
    )
    val radioactivity = mapOf(
        "Bq" to UnitDef("Bq", 1.0), "Ci" to UnitDef("Ci", 3.7e10)
    )
    val kinematicviscosity = mapOf(
        "m2/s" to UnitDef("m2/s", 1.0), "St" to UnitDef("St", 1e-4),
        "cSt" to UnitDef("cSt", 1e-6)
    )
    val rotational = mapOf(
        "rad/s" to UnitDef("rad/s", 1.0), "rpm" to UnitDef("rpm", 0.10471975511965977),
        "rps" to UnitDef("rps", 6.283185307179586)
    )

    val amount = mapOf(
        "pmol" to UnitDef("pmol", 1e-12), "nmol" to UnitDef("nmol", 1e-9),
        "umol" to UnitDef("umol", 1e-6), "mmol" to UnitDef("mmol", 1e-3),
        "mol" to UnitDef("mol", 1.0), "kmol" to UnitDef("kmol", 1000.0),
        "lbmol" to UnitDef("lbmol", 453.59237) // pound-mole (ConvertAll ideas-only),
    )

    /**
     * Blood glucose: 1 mmol/L = 18.0182 mg/dL both ways (clinical molar mass
     * convention for glucose; standard lab factor, exact by stipulation here).
     */
    val glucose = mapOf(
        "mmol/L" to UnitDef("mmol/L", 1.0),
        "mg/dL" to UnitDef("mg/dL", 1.0 / 18.0182)
    )

    /**
     * Running pace, base s/m. All entries are exact definitions:
     * min/km = 60 s per 1000 m, min/mi = 60 s per 1609.344 m, h/km = 3.6 s/m.
     */
    val pace = mapOf(
        "s/m" to UnitDef("s/m", 1.0),
        "min/km" to UnitDef("min/km", 0.06),
        "min/mi" to UnitDef("min/mi", 60.0 / 1609.344),
        "h/km" to UnitDef("h/km", 3.6)
    )

    /**
     * Optical power, base 1/m. Diopter = m^-1 (ConvertAll ideas-only;
     * dpt is the common symbol alias).
     */
    val opticalPower = mapOf(
        "diopter" to UnitDef("diopter", 1.0),
        "dpt" to UnitDef("dpt", 1.0)
    )

    /**
     * Luminance, base nit = cd/m2. Definitions: stilb = 1 cd/cm2 exactly;
     * lambert = cd/(pi cm2) and footlambert = cd/(pi ft2) (ConvertAll
     * ideas-only), hence 10000/pi and 1/(pi ft2 in m2).
     */
    val luminance = mapOf(
        "nit" to UnitDef("nit", 1.0),
        "cd/m2" to UnitDef("cd/m2", 1.0), // nit alias,
        "stilb" to UnitDef("stilb", 10000.0),
        "lambert" to UnitDef("lambert", 10000.0 / Math.PI),
        "footlambert" to UnitDef("footlambert", 1.0 / (Math.PI * 0.09290304))
    )

    /**
     * Magnetic flux, base weber. Maxwell = 1e-8 Wb (ConvertAll ideas-only);
     * Mx is the standard maxwell symbol.
     */
    val magflux = mapOf(
        "Wb" to UnitDef("Wb", 1.0),
        "Mx" to UnitDef("Mx", 1e-8)
    )

    /**
     * Luminous intensity/flux, base candela. Lumen = cd times sr and sr is
     * dimensionless, so both share factor 1.0 here (ConvertAll ideas-only).
     */
    val luminous = mapOf(
        "cd" to UnitDef("cd", 1.0),
        "lm" to UnitDef("lm", 1.0)
    )

    /**
     * SI prefix ladder quetta..quecto for students (ladder idea cross-checked
     * against unitto Prefix collection, GPL ideas-only; values are exact SI).
     * Use [prefixFactor] to look symbols up.
     */
    val siPrefixes = mapOf(
        "Q" to 1e30, "R" to 1e27, "Y" to 1e24, "Z" to 1e21,
        "E" to 1e18, "P" to 1e15, "T" to 1e12, "G" to 1e9,
        "M" to 1e6, "k" to 1e3, "h" to 1e2, "da" to 1e1,
        "d" to 1e-1, "c" to 1e-2, "m" to 1e-3, "u" to 1e-6,
        "n" to 1e-9, "p" to 1e-12, "f" to 1e-15, "a" to 1e-18,
        "z" to 1e-21, "y" to 1e-24, "r" to 1e-27, "q" to 1e-30
    )

    fun prefixFactor(symbol: String): Double {
        val f = siPrefixes[symbol]
        require(f != null) { "unknown SI prefix: $symbol" }
        return f
    }

    /**
     * Browsable number-base table wired to [fromBase] (additive only; never
     * routed through [convert]). Labels are short programmer spellings.
     */
    val numberBases = mapOf("bin" to 2, "oct" to 8, "dec" to 10, "hex" to 16)

    val numberBaseLabels = mapOf(
        "bin" to "binary", "oct" to "octal", "dec" to "decimal", "hex" to "hexadecimal"
    )

    fun convertNumberBase(value: Double, baseId: String): String {
        val base = numberBases[baseId]
        require(base != null) { "unknown number base: $baseId" }
        return fromBase(value, base)
    }

    /**
     * EV efficiency, base mi/kWh. MPGe = 1/33.70 mi/kWh (one gallon of
     * gasoline = 33.70 kWh; ConvertAll ideas-only). Wh/km is reciprocal, so
     * it uses NaN + [convertEv] exactly like [fuel] uses [convertFuel].
     */
    val ev = mapOf(
        "mi/kWh" to UnitDef("mi/kWh", 1.0),
        "MPGe" to UnitDef("MPGe", 1.0 / 33.7),
        "Wh/km" to UnitDef("Wh/km", Double.NaN)
    )

    private val evIds = ev.keys
    private const val WHKM_CONST = 1000.0 / 1.609344 // (mi/kWh) per (Wh/km), reciprocal via exact mile,

    fun evToMiPerKwh(v: Double, from: String): Double = when (from) {
        "Wh/km" -> {
            require(v != 0.0) { "efficiency must be non-zero" }
            WHKM_CONST / v
        }
        else -> v * ev.getValue(from).toBase
    }

    fun miPerKwhToEv(v: Double, to: String): Double = when (to) {
        "Wh/km" -> {
            require(v != 0.0) { "efficiency must be non-zero" }
            WHKM_CONST / v
        }
        else -> v / ev.getValue(to).toBase
    }

    fun convertEv(value: Double, from: String, to: String): Double {
        require(from in evIds) { "unknown EV unit: $from" }
        require(to in evIds) { "unknown EV unit: $to" }
        return miPerKwhToEv(evToMiPerKwh(value, from), to)
    }

    /**
     * Scheduling, base hour. Man-hour = 168/40 h = 4.2 h with man-week = 40
     * man-hours and man-year = 52 man-weeks (ConvertAll ideas-only).
     */
    val scheduling = mapOf(
        "man-hour" to UnitDef("man-hour", 4.2),
        "man-day" to UnitDef("man-day", 33.6),
        "man-week" to UnitDef("man-week", 168.0),
        "man-year" to UnitDef("man-year", 8736.0)
    )

    /**
     * Conductance, base siemens = A/V exactly (ConvertAll ideas-only);
     * mho is the historical alias.
     */
    val conductance = mapOf(
        "S" to UnitDef("S", 1.0),
        "mS" to UnitDef("mS", 0.001),
        "uS" to UnitDef("uS", 1e-6),
        "mho" to UnitDef("mho", 1.0)
    )

    /**
     * Gas flow, base m3/s as volumetric equivalent at standard conditions.
     * sccm/slpm are pressure-times-flow (ConvertAll ideas-only); the linear
     * factors below are the plain cm3/min and L/min volumes, flagged for
     * students, not mass-flow metrology.
     */
    val gasflow = mapOf(
        "m3/s" to UnitDef("m3/s", 1.0),
        "sccm" to UnitDef("sccm", 1e-6 / 60.0),
        "slpm" to UnitDef("slpm", 1e-3 / 60.0)
    )

    /**
     * Thermal resistance, base RSI = K m2/W (ConvertAll ideas-only).
     * R-value = 1 ft2 h F/Btu = 0.1761101838 RSI by exact definition chain
     * (ft2, hour, Btu_IT 1055.05585262 J).
     */
    val rvalue = mapOf(
        "RSI" to UnitDef("RSI", 1.0),
        "Rvalue" to UnitDef("Rvalue", 0.1761101838)
    )

    /**
     * UK gas-mark oven table, gas mark to Celsius (dedicated table lookup,
     * NOT a linear convert: oven steps are uneven). Values retyped from the
     * ConvertAll Gas-mark Kelvin row (GPL ideas-only): GM1 = 140 C through
     * GM9 = 240 C. [cToGasMark] is the nearest-entry reverse lookup.
     */
    val gasMarkTable = mapOf(
        1.0 to 140.0, 2.0 to 150.0, 3.0 to 170.0, 4.0 to 180.0,
        5.0 to 190.0, 6.0 to 200.0, 7.0 to 220.0, 8.0 to 230.0,
        9.0 to 240.0
    )

    fun gasMarkToC(gm: Double): Double {
        require(gm.isFinite()) { "gas mark must be finite" }
        val hit = gasMarkTable[gm]
        require(hit != null) { "gas mark must be one of 1..9" }
        return hit
    }

    fun cToGasMark(c: Double): Double {
        require(c.isFinite()) { "temperature must be finite" }
        return gasMarkTable.minByOrNull { kotlin.math.abs(it.value - c) }?.key ?: 1.0
    }

    // Sound level (dB) intentionally absent: decibels are logarithmic and need a
    // reference quantity, so they cannot use the linear toBase model above.

    private const val KM_S_TO_BASE = 1000.0

    private val fuelIds = fuel.keys
    private const val MPG_US_CONST = 235.214583
    private const val MPG_UK_CONST = 282.481053

    fun fuelToL100km(v: Double, from: String): Double = when (from) {
        "mpg_us", "mpg" -> {
            require(v != 0.0) { "fuel economy must be non-zero" }
            MPG_US_CONST / v
        }
        "mpg_uk", "mpgUK" -> {
            require(v != 0.0) { "fuel economy must be non-zero" }
            MPG_UK_CONST / v
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
        "mpg_uk", "mpgUK" -> {
            require(v != 0.0) { "fuel economy must be non-zero" }
            MPG_UK_CONST / v
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
        val fromEv = from.id in evIds
        val toEv = to.id in evIds
        if (fromEv || toEv) {
            require(fromEv && toEv) { "cannot mix EV efficiency and linear units" }
            return convertEv(value, from.id, to.id)
        }
        // Shoe and ring sizes are affine (offset) scales, so they cannot go
        // through the linear toBase path below; same dispatch shape as fuel.
        val fromShoe = isShoeDef(from)
        val toShoe = isShoeDef(to)
        if (fromShoe || toShoe) {
            require(fromShoe && toShoe) { "cannot mix shoe sizes with other units" }
            return convertShoe(value, from.id, to.id)
        }
        val fromRing = isRingDef(from)
        val toRing = isRingDef(to)
        if (fromRing || toRing) {
            require(fromRing && toRing) { "cannot mix ring sizes with other units" }
            return convertRing(value, from.id, to.id)
        }
        require(from.toBase.isFinite()) { "source factor must be finite" }
        require(to.toBase.isFinite()) { "target factor must be finite" }
        require(to.toBase != 0.0) { "target factor must be non-zero" }
        return value * from.toBase / to.toBase
    }

    fun convertTemp(v: Double, from: String, to: String): Double {
        require(from in temperature) { "unknown temperature unit: $from" }
        require(to in temperature) { "unknown temperature unit: $to" }
        if (from == to) return v
        // Affine scales route via Celsius (formulas retyped from
        // unitconverterultimate TemperatureConverter, Apache-2.0).
        val c = when (from) {
            "C" -> v
            "F" -> (v - 32) * 5 / 9
            "K" -> v - 273.15
            "R" -> (v - 491.67) * 5 / 9
            "Reaumur" -> v * 5 / 4
            "Romer" -> (v - 7.5) * 40 / 21
            "Delisle" -> 100 - v * 2 / 3
            "Newton" -> v * 100 / 33
            else -> v
        }
        return when (to) {
            "C" -> c
            "F" -> c * 9 / 5 + 32
            "K" -> c + 273.15
            "R" -> (c + 273.15) * 9 / 5
            "Reaumur" -> c * 4 / 5
            "Romer" -> c * 21 / 40 + 7.5
            "Delisle" -> (100 - c) * 3 / 2
            "Newton" -> c * 33 / 100
            else -> c
        }
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
