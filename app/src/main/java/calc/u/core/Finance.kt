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

    // Finance Pro tools — formulas inspired by CalcHub (MIT Licensed), re-implemented from scratch.
    fun stockAverage(shares1: Double, price1: Double, shares2: Double, price2: Double): Triple<Double, Double, Double> {
        require(shares1 >= 0.0) { "shares1 must be >= 0" }
        require(shares2 >= 0.0) { "shares2 must be >= 0" }
        require(price1 >= 0.0) { "price1 must be >= 0" }
        require(price2 >= 0.0) { "price2 must be >= 0" }
        val totalShares = shares1 + shares2
        val totalCost = shares1 * price1 + shares2 * price2
        val avgPrice = if (totalShares == 0.0) 0.0 else totalCost / totalShares
        return Triple(totalShares, avgPrice, totalCost)
    }

    fun depreciationSL(cost: Double, salvage: Double, lifeYears: Double): Double {
        require(cost >= 0.0) { "cost must be >= 0" }
        require(salvage >= 0.0) { "salvage must be >= 0" }
        require(lifeYears > 0.0) { "lifeYears must be > 0" }
        return (cost - salvage) / lifeYears
    }

    fun depreciationSL(cost: Double, salvage: Double, lifeYears: Int): Double =
        depreciationSL(cost, salvage, lifeYears.toDouble())

    fun depreciationDB(cost: Double, ratePct: Double, year: Int): Double {
        require(cost >= 0.0) { "cost must be >= 0" }
        require(ratePct >= 0.0) { "ratePct must be >= 0" }
        require(year >= 0) { "year must be >= 0" }
        return cost * (1 - ratePct / 100).pow(year)
    }

    fun depreciationDB(cost: Double, ratePct: Double, year: Double): Double {
        require(cost >= 0.0) { "cost must be >= 0" }
        require(ratePct >= 0.0) { "ratePct must be >= 0" }
        require(year >= 0.0) { "year must be >= 0" }
        return cost * (1 - ratePct / 100).pow(year)
    }

    fun savingsGoal(monthly: Double, annualPct: Double, years: Double): Double {
        require(monthly >= 0.0) { "monthly must be >= 0" }
        require(years > 0.0) { "years must be > 0" }
        val n = (years * 12).toInt()
        require(n > 0) { "years must be > 0" }
        val r = annualPct / 1200
        return if (r == 0.0) monthly * n else monthly * ((1 + r).pow(n) - 1) / r
    }

    fun savingsGoal(monthly: Double, annualPct: Double, years: Int): Double =
        savingsGoal(monthly, annualPct, years.toDouble())

    fun rentVsBuy(monthlyRent: Double, rentGrowthPct: Double, homePrice: Double, downPct: Double, mortgageRatePct: Double, years: Int): Pair<Double, Double> {
        require(monthlyRent >= 0.0) { "monthlyRent must be >= 0" }
        require(homePrice >= 0.0) { "homePrice must be >= 0" }
        require(downPct >= 0.0) { "downPct must be >= 0" }
        require(years > 0) { "years must be > 0" }
        val g = rentGrowthPct / 100
        var totalRent = 0.0
        for (y in 0 until years) {
            totalRent += monthlyRent * 12 * (1 + g).pow(y)
        }
        val down = homePrice * downPct / 100
        val principal = homePrice - down
        val months = years * 12
        val payment = emi(principal, mortgageRatePct, months)
        val totalBuy = down + payment * months
        return Pair(totalRent, totalBuy)
    }

    fun rentVsBuy(monthlyRent: Double, rentGrowthPct: Double, homePrice: Double, downPct: Double, mortgageRatePct: Double, years: Double): Pair<Double, Double> {
        require(monthlyRent >= 0.0) { "monthlyRent must be >= 0" }
        require(homePrice >= 0.0) { "homePrice must be >= 0" }
        require(downPct >= 0.0) { "downPct must be >= 0" }
        require(years > 0.0) { "years must be > 0" }
        val nYears = years.toInt()
        require(nYears > 0) { "years must be > 0" }
        return rentVsBuy(monthlyRent, rentGrowthPct, homePrice, downPct, mortgageRatePct, nYears)
    }

    fun rule72(ratePct: Double): Double {
        require(ratePct > 0.0) { "ratePct must be > 0" }
        return 72 / ratePct
    }

    fun gpa(grades: List<Pair<Double, Double>>): Double {
        var points = 0.0
        var credits = 0.0
        for ((grade, credit) in grades) {
            require(grade >= 0.0) { "grade must be >= 0" }
            require(credit >= 0.0) { "credits must be >= 0" }
            points += grade * credit
            credits += credit
        }
        if (credits == 0.0) return 0.0
        return points / credits
    }

    fun gradeNeeded(currentPct: Double, weightDonePct: Double, targetPct: Double): Double {
        require(weightDonePct >= 0.0) { "weightDonePct must be >= 0" }
        require(weightDonePct < 100.0) { "weightDonePct must be < 100" }
        val done = weightDonePct / 100
        val left = 1 - done
        return (targetPct - currentPct * done) / left
    }

    fun paycheck(hourlyRate: Double, hoursPerWeek: Double, taxPct: Double): Triple<Double, Double, Double> {
        require(hourlyRate >= 0.0) { "hourlyRate must be >= 0" }
        require(hoursPerWeek >= 0.0) { "hoursPerWeek must be >= 0" }
        require(taxPct >= 0.0) { "taxPct must be >= 0" }
        val gross = hourlyRate * hoursPerWeek * 52 / 12
        val tax = gross * taxPct / 100
        return Triple(gross, tax, gross - tax)
    }

    fun creditPayoff(balance: Double, aprPct: Double, monthlyPayment: Double): Triple<Int, Double, Double> {
        require(balance >= 0.0) { "balance must be >= 0" }
        require(aprPct >= 0.0) { "aprPct must be >= 0" }
        require(monthlyPayment > 0.0) { "monthlyPayment must be > 0" }
        if (balance == 0.0) return Triple(0, 0.0, 0.0)
        val r = aprPct / 1200
        if (r == 0.0) {
            val months = kotlin.math.ceil(balance / monthlyPayment).toInt()
            return Triple(months, 0.0, balance)
        }
        var bal = balance
        var months = 0
        var totalInterest = 0.0
        var totalPaid = 0.0
        var guard = 0
        while (bal > 0) {
            val interest = bal * r
            require(monthlyPayment > interest) { "payment must exceed monthly interest" }
            months += 1
            totalInterest += interest
            if (bal + interest <= monthlyPayment) {
                totalPaid += bal + interest
                bal = 0.0
            } else {
                bal = bal + interest - monthlyPayment
                totalPaid += monthlyPayment
            }
            guard += 1
            require(guard <= 10000) { "payoff did not converge" }
        }
        return Triple(months, totalInterest, totalPaid)
    }

    fun loanCompare(principal: Double, rateA: Double, rateB: Double, months: Int): Triple<Double, Double, Double> {
        require(principal >= 0.0) { "principal must be >= 0" }
        require(months > 0) { "months must be > 0" }
        val emiA = emi(principal, rateA, months)
        val emiB = emi(principal, rateB, months)
        val savingsTotal = (emiB - emiA) * months
        return Triple(emiA, emiB, savingsTotal)
    }

    fun profitMargin(cost: Double, price: Double): Double {
        require(cost >= 0.0) { "cost must be >= 0" }
        require(price >= 0.0) { "price must be >= 0" }
        if (price == 0.0) return 0.0
        return (price - cost) / price * 100
    }

    fun zakat(cashGoldSilver: Double, debts: Double, nisabThreshold: Double = 0.0): Double {
        val net = (cashGoldSilver - debts).coerceAtLeast(0.0)
        if (net < nisabThreshold) return 0.0
        return net * 2.5 / 100
    }
}
