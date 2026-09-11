package calc.u.core

import kotlin.math.*

object Finance {
    fun tip(total: Double, percent: Double, split: Int): Triple<Double, Double, Double> {
        val tipAmt = total * percent / 100
        val grand = total + tipAmt
        return Triple(tipAmt, grand, grand / maxOf(1, split))
    }

    fun withTax(amount: Double, rate: Double, inclusive: Boolean): Pair<Double, Double> {
        return if (inclusive) {
            val net = amount / (1 + rate / 100)
            Pair(net, amount - net)
        } else {
            val tax = amount * rate / 100
            Pair(amount + tax, tax)
        }
    }

    fun emi(principal: Double, annualRate: Double, months: Int): Double {
        if (months <= 0) return 0.0
        val r = annualRate / 1200
        if (r == 0.0) return principal / months
        val f = (1 + r).pow(months)
        return principal * r * f / (f - 1)
    }

    fun compound(principal: Double, annualRate: Double, years: Double, perYear: Int = 12): Double =
        principal * (1 + annualRate / 100 / perYear).pow(perYear * years)

    fun simple(principal: Double, annualRate: Double, years: Double): Pair<Double, Double> {
        val interest = principal * annualRate / 100 * years
        return Pair(interest, principal + interest)
    }

    fun unitPrice(price: Double, qty: Double): Double = if (qty == 0.0) 0.0 else price / qty
}
