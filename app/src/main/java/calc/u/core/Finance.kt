package calc.u.core

import kotlin.math.pow

object Finance {
    fun tip(total: Double, percent: Double, split: Int): Triple<Double, Double, Double> {
        val tipAmt = total * percent / 100
        val grand = total + tipAmt
        return Triple(tipAmt, grand, grand / if (split > 1) split else 1)
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

    fun sip(monthly: Double, annualPct: Double, years: Double): Triple<Double, Double, Double> {
        require(monthly >= 0.0) { "monthly must be >= 0" }
        require(years > 0.0) { "years must be > 0" }
        val n = (years * 12).toInt()
        require(n > 0) { "years must be > 0" }
        val invested = monthly * n
        val r = annualPct / 1200
        val total = if (r == 0.0) invested
        else monthly * ((1 + r).pow(n) - 1) / r * (1 + r)
        return Triple(invested, total - invested, total)
    }

    fun cagr(initial: Double, final: Double, years: Double): Double {
        require(initial > 0.0) { "initial must be > 0" }
        require(final >= 0.0) { "final must be >= 0" }
        require(years > 0.0) { "years must be > 0" }
        return (final / initial).pow(1.0 / years) - 1
    }

    fun fd(principal: Double, ratePct: Double, years: Double, freqPerYear: Int = 4): Triple<Double, Double, Double> {
        require(principal >= 0.0) { "principal must be >= 0" }
        require(years >= 0.0) { "years must be >= 0" }
        require(freqPerYear > 0) { "freqPerYear must be > 0" }
        val total = principal * (1 + ratePct / 100 / freqPerYear).pow(freqPerYear * years)
        return Triple(principal, total - principal, total)
    }

    fun vat(amount: Double, pct: Double, incl: Boolean): Triple<Double, Double, Double> {
        require(pct >= 0.0) { "pct must be >= 0" }
        return if (incl) {
            val net = amount / (1 + pct / 100)
            Triple(net, amount - net, amount)
        } else {
            val tax = amount * pct / 100
            Triple(amount, tax, amount + tax)
        }
    }

    fun inflation(present: Double, ratePct: Double, years: Double): Double {
        require(years >= 0.0) { "years must be >= 0" }
        return present * (1 + ratePct / 100).pow(years)
    }

    fun amortization(principal: Double, annualRatePct: Double, months: Int): List<Triple<Int, Double, Double>> {
        require(months > 0) { "months must be > 0" }
        require(principal >= 0.0) { "principal must be >= 0" }
        val r = annualRatePct / 1200
        val payment = emi(principal, annualRatePct, months)
        var balance = principal
        val out = ArrayList<Triple<Int, Double, Double>>(months)
        for (m in 1..months) {
            val interest = if (r == 0.0) 0.0 else balance * r
            var principalPaid = payment - interest
            if (m == months) principalPaid = balance
            out.add(Triple(m, principalPaid, interest))
            balance -= principalPaid
        }
        return out
    }
}
