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
            val denom = 1 + rate / 100
            require(denom != 0.0) { "rate must not be -100" }
            val net = amount / denom
            Pair(net, amount - net)
        } else {
            val tax = amount * rate / 100
            Pair(amount + tax, tax)
        }
    }

    fun emi(principal: Double, annualRate: Double, months: Int): Double {
        require(months > 0) { "months must be > 0" }
        val r = annualRate / 1200
        if (r == 0.0) return principal / months
        val f = (1 + r).pow(months)
        if (f == 1.0) return principal / months
        return principal * r * f / (f - 1)
    }

    fun compound(principal: Double, annualRate: Double, years: Double, perYear: Int = 12): Double {
        require(perYear > 0) { "perYear must be > 0" }
        return principal * (1 + annualRate / 100 / perYear).pow(perYear * years)
    }

    fun simple(principal: Double, annualRate: Double, years: Double): Pair<Double, Double> {
        val interest = principal * annualRate / 100 * years
        return Pair(interest, principal + interest)
    }

    fun unitPrice(price: Double, qty: Double): Double {
        require(qty != 0.0) { "qty must not be 0" }
        return price / qty
    }

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
        require(months <= 1200) { "months must be <= 1200" }
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

    /**
     * Declining-balance depreciation CHARGE for a single year (not book value):
     * charge(year) = cost * rate * (1 - rate)^(year-1), with rate = ratePct / 100.
     * Book value after N years = cost - sum of charges = cost * (1 - rate)^N;
     * see [depreciationDBBook] for the book-value form.
     */
    fun depreciationDB(cost: Double, ratePct: Double, year: Int): Double {
        require(cost >= 0.0) { "cost must be >= 0" }
        require(ratePct >= 0.0) { "ratePct must be >= 0" }
        require(year >= 1) { "year must be >= 1" }
        val rate = ratePct / 100
        return cost * rate * (1 - rate).pow(year - 1)
    }

    /**
     * Same as [depreciationDB] but accepts a fractional year, which is rounded
     * to the nearest whole year (never truncated).
     */
    fun depreciationDB(cost: Double, ratePct: Double, year: Double): Double {
        require(cost >= 0.0) { "cost must be >= 0" }
        require(ratePct >= 0.0) { "ratePct must be >= 0" }
        require(year >= 1.0) { "year must be >= 1" }
        return depreciationDB(cost, ratePct, kotlin.math.round(year).toInt().coerceAtLeast(1))
    }

    /**
     * Declining-balance BOOK VALUE after [year] full years:
     * book(year) = cost * (1 - rate)^year. Companion to [depreciationDB],
     * which returns the per-year charge instead.
     */
    fun depreciationDBBook(cost: Double, ratePct: Double, year: Int): Double {
        require(cost >= 0.0) { "cost must be >= 0" }
        require(ratePct >= 0.0) { "ratePct must be >= 0" }
        require(year >= 0) { "year must be >= 0" }
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
        require(downPct <= 100.0) { "downPct must be <= 100" }
        require(years > 0) { "years must be > 0" }
        require(years <= 100) { "years must be <= 100" }
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

    /**
     * Fractional [years] are rounded to the nearest whole year (never truncated).
     */
    fun rentVsBuy(monthlyRent: Double, rentGrowthPct: Double, homePrice: Double, downPct: Double, mortgageRatePct: Double, years: Double): Pair<Double, Double> {
        require(monthlyRent >= 0.0) { "monthlyRent must be >= 0" }
        require(homePrice >= 0.0) { "homePrice must be >= 0" }
        require(downPct >= 0.0) { "downPct must be >= 0" }
        require(years > 0.0) { "years must be > 0" }
        require(years <= 100.0) { "years must be <= 100" }
        val nYears = kotlin.math.round(years).toInt()
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
        if (credits == 0.0) throw IllegalArgumentException("total credits must be > 0")
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
        require(taxPct <= 100.0) { "taxPct must be <= 100" }
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
        require(monthlyPayment > balance * r) { "monthlyPayment must exceed first month interest" }
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
        require(price != 0.0) { "price must not be 0" }
        return (price - cost) / price * 100
    }

    /**
     * Zakat due at 2.5% of net assets above nisab. [nisabThreshold] must be
     * > 0: pass the current nisab value for your school/currency; a missing
     * nisab must never silently charge (or waive) zakat.
     */
    fun zakat(cashGoldSilver: Double, debts: Double, nisabThreshold: Double = 0.0): Double {
        require(nisabThreshold > 0.0) { "nisabThreshold must be > 0" }
        val net = (cashGoldSilver - debts).coerceAtLeast(0.0)
        if (net < nisabThreshold) return 0.0
        return net * 2.5 / 100
    }

    // Date-range ROI, re-implemented from scratch.
    // Returns Triple(profit, marginPct, annualizedPct).
    fun investRoi(invested: Double, settled: Double, days: Int): Triple<Double, Double, Double> {
        require(invested > 0.0) { "invested must be > 0" }
        require(days > 0) { "days must be > 0" }
        require(settled >= 0.0) { "settled must be >= 0" }
        val profit = settled - invested
        val marginPct = profit / invested * 100
        val annualizedPct = (settled / invested).pow(365.0 / days) - 1.0
        return Triple(profit, marginPct, annualizedPct * 100)
    }

    fun investRoi(invested: Double, settled: Double, days: Double): Triple<Double, Double, Double> {
        require(days > 0.0) { "days must be > 0" }
        require(days <= Int.MAX_VALUE) { "days must be <= Int.MAX" }
        return investRoi(invested, settled, kotlin.math.round(days).toInt().coerceAtLeast(1))
    }

    // GST forward: Triple(net, tax, gross). If intra is true the tax splits
    // equally into CGST/SGST, otherwise it is all IGST; returned tax is the total.
    fun gstForward(net: Double, ratePct: Double, intra: Boolean): Triple<Double, Double, Double> {
        require(net >= 0.0) { "net must be >= 0" }
        require(ratePct >= 0.0) { "ratePct must be >= 0" }
        val tax = net * ratePct / 100
        return Triple(net, tax, net + tax)
    }

    // GST reverse: Triple(net, tax, gross).
    fun gstReverse(gross: Double, ratePct: Double): Triple<Double, Double, Double> {
        require(gross >= 0.0) { "gross must be >= 0" }
        require(ratePct >= 0.0) { "ratePct must be >= 0" }
        val net = gross / (1 + ratePct / 100)
        return Triple(net, gross - net, gross)
    }

    // Finance+ core, re-implemented from scratch.
    fun discountForward(mrp: Double, pct: Double): Pair<Double, Double> {
        require(mrp >= 0.0) { "mrp must be >= 0" }
        require(pct >= 0.0 && pct <= 100.0) { "pct must be in 0..100" }
        val final = mrp * (1 - pct / 100)
        return Pair(final, mrp - final)
    }

    fun discountReverse(mrp: Double, final: Double): Pair<Double, Double> {
        require(mrp > 0.0) { "mrp must be > 0" }
        require(final >= 0.0) { "final must be >= 0" }
        require(final <= mrp) { "final must be <= mrp" }
        val savings = mrp - final
        return Pair(savings / mrp * 100, savings)
    }

    fun tipRoundUp(bill: Double, tipAmount: Double, people: Int): Triple<Double, Double, Double> {
        require(bill >= 0.0) { "bill must be >= 0" }
        require(tipAmount >= 0.0) { "tipAmount must be >= 0" }
        require(people >= 1) { "people must be >= 1" }
        val total = bill + tipAmount
        val per = kotlin.math.ceil(total / people)
        val roundedTotal = per * people
        return Triple(per, roundedTotal, roundedTotal - bill)
    }

    /**
     * Compound growth with [timesPerYear] payouts per year.
     * Returns Triple(principal, maturity, gain).
     * NOTE: this order differs from [investRoi], which returns
     * Triple(profit, marginPct, annualizedPct). The order is kept as-is
     * because UI code destructures it; do not reorder.
     */
    fun investFreq(principal: Double, annualPct: Double, years: Double, timesPerYear: Int): Triple<Double, Double, Double> {
        require(principal >= 0.0) { "principal must be >= 0" }
        require(years >= 0.0) { "years must be >= 0" }
        require(timesPerYear > 0) { "timesPerYear must be > 0" }
        val maturity = principal * (1 + annualPct / 100 / timesPerYear).pow(timesPerYear * years)
        return Triple(principal, maturity, maturity - principal)
    }

    fun investFreq(principal: Double, annualPct: Double, years: Int, timesPerYear: Int): Triple<Double, Double, Double> =
        investFreq(principal, annualPct, years.toDouble(), timesPerYear)

    fun daysToBirthday(month: Int, day: Int, todayEpochDay: Long): Long {
        require(month in 1..12) { "month must be in 1..12" }
        require(day in 1..31) { "day must be in 1..31" }
        if (month == 2) require(day <= 29) { "day must be <= 29 for February" }
        if (month == 4 || month == 6 || month == 9 || month == 11) require(day <= 30) { "day must be <= 30" }
        val today = java.time.LocalDate.ofEpochDay(todayEpochDay)
        fun birthdayIn(year: Int): java.time.LocalDate {
            if (month == 2 && day == 29 && !java.time.Year.isLeap(year.toLong())) {
                return java.time.LocalDate.of(year, 2, 28)
            }
            return java.time.LocalDate.of(year, month, day)
        }
        var next = birthdayIn(today.year)
        if (next.isBefore(today)) {
            next = birthdayIn(today.year + 1)
        }
        return java.time.temporal.ChronoUnit.DAYS.between(today, next)
    }

    fun dateOffset(dateIso: String, offsetDays: Int): String {
        return java.time.LocalDate.parse(dateIso).plusDays(offsetDays.toLong()).toString()
    }

    fun dateOffset(dateIso: String, offsetDays: Long): String {
        return java.time.LocalDate.parse(dateIso).plusDays(offsetDays).toString()
    }

    fun timezoneConvert(timeStr: String, fromZone: String, toZone: String): String {
        val from: java.time.ZoneId = try {
            java.time.ZoneId.of(fromZone)
        } catch (e: Exception) {
            throw IllegalArgumentException("bad zone: $fromZone")
        }
        val to: java.time.ZoneId = try {
            java.time.ZoneId.of(toZone)
        } catch (e: Exception) {
            throw IllegalArgumentException("bad zone: $toZone")
        }
        val time = java.time.LocalTime.parse(timeStr)
        val date = java.time.LocalDate.now(from)
        val zonedFrom = java.time.ZonedDateTime.of(date, time, from)
        val zonedTo = zonedFrom.withZoneSameInstant(to)
        return zonedTo.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    }
}
