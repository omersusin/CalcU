package calc.u.core

import java.util.Locale
import kotlin.math.floor
import kotlin.math.round

object Electro {
    private val digits = mapOf(
        "black" to 0,
        "brown" to 1,
        "red" to 2,
        "orange" to 3,
        "yellow" to 4,
        "green" to 5,
        "blue" to 6,
        "violet" to 7,
        "gray" to 8,
        "grey" to 8,
        "white" to 9
    )

    private val multipliers = mapOf(
        "black" to 1.0,
        "brown" to 10.0,
        "red" to 100.0,
        "orange" to 1e3,
        "yellow" to 1e4,
        "green" to 1e5,
        "blue" to 1e6,
        "violet" to 1e7,
        "gray" to 1e8,
        "grey" to 1e8,
        "white" to 1e9,
        "gold" to 0.1,
        "silver" to 0.01
    )

    private val tolerances = mapOf(
        "brown" to 1.0,
        "red" to 2.0,
        "green" to 0.5,
        "blue" to 0.25,
        "violet" to 0.1,
        "gray" to 0.05,
        "grey" to 0.05,
        "gold" to 5.0,
        "silver" to 10.0,
        "none" to 20.0
    )

    fun decode4Band(colors: List<String>): String {
        require(colors.size == 4) { "4-band needs exactly 4 colors" }
        val n = colors.map { it.trim().lowercase() }
        val d1 = digits[n[0]]
        require(d1 != null) { "invalid digit color: ${colors[0]}" }
        val d2 = digits[n[1]]
        require(d2 != null) { "invalid digit color: ${colors[1]}" }
        val mult = multipliers[n[2]]
        require(mult != null) { "invalid multiplier color: ${colors[2]}" }
        val tol = tolerances[n[3]]
        require(tol != null) { "invalid tolerance color: ${colors[3]}" }
        return formatResult((d1 * 10 + d2) * mult, tol)
    }

    fun decode5Band(colors: List<String>): String {
        require(colors.size == 5) { "5-band needs exactly 5 colors" }
        val n = colors.map { it.trim().lowercase() }
        val d1 = digits[n[0]]
        require(d1 != null) { "invalid digit color: ${colors[0]}" }
        val d2 = digits[n[1]]
        require(d2 != null) { "invalid digit color: ${colors[1]}" }
        val d3 = digits[n[2]]
        require(d3 != null) { "invalid digit color: ${colors[2]}" }
        val mult = multipliers[n[3]]
        require(mult != null) { "invalid multiplier color: ${colors[3]}" }
        val tol = tolerances[n[4]]
        require(tol != null) { "invalid tolerance color: ${colors[4]}" }
        return formatResult((d1 * 100 + d2 * 10 + d3) * mult, tol)
    }

    fun voltageDivider(vin: Double, r1: Double, r2: Double): Double {
        require(vin.isFinite()) { "vin must be finite" }
        require(r1 > 0) { "r1 must be > 0" }
        require(r2 > 0) { "r2 must be > 0" }
        return vin * r2 / (r1 + r2)
    }

    fun ledResistor(vsupply: Double, vf: Double, maMilliamps: Double): Double {
        require(vsupply.isFinite()) { "vsupply must be finite" }
        require(vf >= 0) { "vf must be >= 0" }
        require(maMilliamps > 0) { "maMilliamps must be > 0" }
        require(vsupply > vf) { "vsupply must exceed vf" }
        return (vsupply - vf) / (maMilliamps / 1000.0)
    }

    fun rcTimeConstant(rOhms: Double, cFarads: Double): Double {
        require(rOhms > 0) { "rOhms must be > 0" }
        require(cFarads > 0) { "cFarads must be > 0" }
        return rOhms * cFarads
    }

    private fun formatResult(ohms: Double, tolerancePct: Double): String =
        "${formatOhms(ohms)} ±${formatTol(tolerancePct)}%"

    private fun formatTol(p: Double): String =
        if (p == floor(p)) p.toLong().toString() else p.toString()

    private fun formatOhms(ohms: Double): String = when {
        ohms >= 1e6 -> "${compact(ohms / 1e6)}MΩ"
        ohms >= 1e3 -> "${compact(ohms / 1e3)}kΩ"
        else -> "${compact(ohms)}Ω"
    }

    private fun compact(v: Double): String {
        val r = round(v * 100) / 100.0
        return if (r.isFinite() && r == floor(r)) r.toLong().toString()
        else String.format(Locale.US, "%.2f", r).trimEnd('0').trimEnd('.')
    }
}
