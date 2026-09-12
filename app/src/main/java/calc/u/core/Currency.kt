package calc.u.core

object Currency {
    data class Rate(val code: String, val rateToUsd: Double)

    val codes = listOf(
        "USD", "EUR", "GBP", "JPY", "INR", "CNY", "TRY", "AED", "SAR", "PKR",
        "BDT", "IDR", "MYR", "PHP", "THB", "VND", "KRW", "AUD", "CAD", "CHF",
        "SEK", "NOK", "DKK", "PLN", "CZK", "HUF", "RON", "BGN", "RSD",
        "UAH", "RUB", "KZT", "AZN", "GEL", "AMD", "BYN", "MDL", "EGP", "NGN",
        "KES", "ZAR", "MAD", "DZD", "TND", "IQD", "JOD", "KWD", "BHD", "QAR",
        "OMR", "YER", "LBP", "SYP", "AFN", "IRR", "BRL", "MXN", "ARS", "CLP",
        "COP", "PEN", "SGD", "HKD", "TWD", "NZD", "NPR", "LKR", "MMK", "KHR",
        "LAK", "BND", "FJD", "ILS", "MVR", "ALL", "BAM", "MKD", "ISK"
    ).distinct().sorted()

    val fallbackUsdRates = mapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "JPY" to 149.5, "ISK" to 138.5,
        "INR" to 83.2, "CNY" to 7.24, "TRY" to 32.8, "AED" to 3.6725
    )

    fun convert(amount: Double, fromRate: Double, toRate: Double): Double {
        require(fromRate != 0.0) { "fromRate must not be 0 (unknown rate for source currency)" }
        return amount / fromRate * toRate
    }

    fun convert(amount: Double, from: Rate, to: Rate): Double =
        convert(amount, from.rateToUsd, to.rateToUsd)
}
