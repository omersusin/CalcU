package calc.u.core

data class Const(val symbol: String, val name: String, val value: Double)

object Constants {
    val mathConsts: List<Const> = listOf(
        Const("π", "pi", 3.141592653589793),
        Const("τ", "tau (2 pi)", 6.283185307179586),
        Const("e", "Euler number", 2.718281828459045),
        Const("φ", "golden ratio", 1.618033988749895),
        Const("γ", "Euler-Mascheroni constant", 0.5772156649015329),
        Const("G", "Catalan constant", 0.915965594177219),
        Const("ζ(3)", "Apery constant", 1.202056903159594),
        Const("A", "Glaisher-Kinkelin constant", 1.2824271291006226),
        Const("√2", "square root of 2", 1.4142135623730951),
        Const("√3", "square root of 3", 1.7320508075688772),
        Const("√5", "square root of 5", 2.23606797749979),
        Const("ln 2", "natural log of 2", 0.6931471805599453),
        Const("ln 10", "natural log of 10", 2.302585092994046),
        Const("log₂e", "log base 2 of e", 1.4426950408889634),
        Const("log₁₀e", "log base 10 of e", 0.4342944819032518),
        Const("K", "Khinchin constant", 2.685452001065306)
    )

    val physicsConsts: List<Const> = listOf(
        Const("c", "speed of light in vacuum (m/s)", 299792458.0),
        Const("h", "Planck constant (J⋅s)", 6.62607015e-34),
        Const("ħ", "reduced Planck constant (J⋅s)", 1.054571817e-34),
        Const("kB", "Boltzmann constant (J/K)", 1.380649e-23),
        Const("G", "gravitational constant (m³/kg⋅s²)", 6.67430e-11),
        Const("NA", "Avogadro constant (1/mol)", 6.02214076e23),
        Const("R", "molar gas constant (J/mol⋅K)", 8.314462618),
        Const("e", "elementary charge (C)", 1.602176634e-19),
        Const("α", "fine-structure constant", 7.2973525693e-3),
        Const("ε₀", "vacuum permittivity (F/m)", 8.8541878128e-12),
        Const("μ₀", "vacuum permeability (N/A²)", 1.25663706212e-6),
        Const("me", "electron mass (kg)", 9.1093837015e-31),
        Const("mp", "proton mass (kg)", 1.67262192369e-27),
        Const("mn", "neutron mass (kg)", 1.67492749804e-27),
        Const("σ", "Stefan-Boltzmann constant (W/m²⋅K⁴)", 5.670374419e-8),
        Const("g", "standard gravity (m/s²)", 9.80665),
        Const("eV", "electronvolt (J)", 1.602176634e-19),
        Const("u", "atomic mass unit (kg)", 1.66053906660e-27),
        Const("ke", "Coulomb constant (N⋅m²/C²)", 8.9875517923e9),
        Const("R∞", "Rydberg constant (1/m)", 10973731.568160)
    )

    val all: List<Const> = mathConsts + physicsConsts

    fun search(q: String): List<Const> {
        val needle = q.trim().lowercase()
        if (needle.isEmpty()) return all
        return all.filter {
            it.symbol.lowercase().contains(needle) || it.name.lowercase().contains(needle)
        }
    }
}
