package calc.u.core

object Currency {
    val codes = listOf(
        "USD","EUR","GBP","JPY","INR","CNY","TRY","AED","SAR","PKR",
        "BDT","IDR","MYR","PHP","THB","VND","KRW","AUD","CAD","CHF",
        "SEK","NOK","DKK","PLN","CZK","HUF","RON","BGN","HRK","RSD",
        "UAH","RUB","KZT","AZN","GEL","AMD","BYN","MDL","EGP","NGN",
        "KES","ZAR","MAD","DZD","TND","IQD","JOD","KWD","BHD","QAR",
        "OMR","YER","LBP","SYP","AFN","IRR","BRL","MXN","ARS","CLP",
        "COP","PEN","SGD","HKD","TWD","NZD","NPR","LKR","MMK","KHR",
        "LAK","BND","FJD","ILS","KWD","MVR","QAR","ALL","BAM","MKD"
    ).distinct().sorted()

    val fallbackUsdRates = mapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "JPY" to 149.5,
        "INR" to 83.2, "CNY" to 7.24, "TRY" to 32.8, "AED" to 3.6725
    )

    fun convert(amount: Double, fromUsdRate: Double, toUsdRate: Double): Double {
        if (fromUsdRate == 0.0) return 0.0
        return amount / fromUsdRate * toUsdRate
    }
}
