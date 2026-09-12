package calc.u

import calc.u.core.Finance
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

class FinanceTest {
    @Test fun sipGrows() {
        val (invested, gain, total) = Finance.sip(5000.0, 12.0, 10.0)
        assertEquals(600000.0, invested, 1e-6)
        assertTrue(total > invested)
        assertTrue(gain > 0)
        assertEquals(invested + gain, total, 1e-6)
    }
    @Test fun cagrDoubling() {
        assertEquals(0.0718, Finance.cagr(100.0, 200.0, 10.0), 0.001)
    }
    @Test fun fdExceedsPrincipal() {
        val (p, interest, total) = Finance.fd(10000.0, 6.0, 5.0, 4)
        assertEquals(10000.0, p, 1e-9)
        assertTrue(total > p)
        assertTrue(interest > 0)
    }
    @Test fun vatRoundTrip() {
        val (_, tax, gross) = Finance.vat(100.0, 10.0, false)
        assertEquals(10.0, tax, 1e-9)
        assertEquals(110.0, gross, 1e-9)
        val (net, tax2, _) = Finance.vat(gross, 10.0, true)
        assertEquals(100.0, net, 1e-9)
        assertEquals(10.0, tax2, 1e-9)
    }
    @Test fun inflationPositive() {
        val f = Finance.inflation(100.0, 5.0, 10.0)
        assertTrue(f > 100.0)
        assertEquals(100.0 * Math.pow(1.05, 10.0), f, 1e-6)
    }
    @Test fun stockAverageDown() {
        val (shares, avg, cost) = Finance.stockAverage(100.0, 10.0, 100.0, 6.0)
        assertEquals(200.0, shares, 1e-9)
        assertEquals(8.0, avg, 1e-9)
        assertEquals(1600.0, cost, 1e-9)
    }
    @Test fun depreciationSLBasic() {
        assertEquals(1000.0, Finance.depreciationSL(10000.0, 1000.0, 9.0), 1e-9)
    }
    @Test fun depreciationDBDeclines() {
        val book = Finance.depreciationDB(10000.0, 20.0, 1)
        assertEquals(8000.0, book, 1e-6)
        assertTrue(Finance.depreciationDB(10000.0, 20.0, 2) < book)
    }
    @Test fun savingsGoalPositive() {
        val fv = Finance.savingsGoal(500.0, 6.0, 10.0)
        assertTrue(fv > 0)
        assertTrue(fv >= 500.0 * 12 * 10)
    }
    @Test fun rule72Eight() {
        assertEquals(9.0, Finance.rule72(8.0), 1e-9)
    }
    @Test fun rentVsBuyPositive() {
        val (rent, buy) = Finance.rentVsBuy(1500.0, 3.0, 300000.0, 20.0, 6.0, 10)
        assertTrue(rent > 0)
        assertTrue(buy > 0)
    }

    @Test fun depreciationDBBookDecreases() {
        val y1 = Finance.depreciationDB(10000.0, 20.0, 1)
        assertEquals(8000.0, y1, 1e-6)
        val y2 = Finance.depreciationDB(10000.0, 20.0, 2)
        assertEquals(6400.0, y2, 1e-6)
        assertTrue(y2 < y1)
    }

    @Test fun savingsGoalSevenPctTenYears() {
        val r = 0.07 / 12
        val n = 120
        val expected = 500 * (((1 + r).pow(n) - 1) / r)
        assertEquals(expected, Finance.savingsGoal(500.0, 7.0, 10.0), 1.0)
    }

    @Test fun inflationThreePctTenYears() {
        assertEquals(134.39, Finance.inflation(100.0, 3.0, 10.0), 0.01)
    }

    @Test fun gpaWeighted() {
        assertEquals(3.5, Finance.gpa(listOf(4.0 to 3.0, 3.0 to 3.0)), 1e-9)
    }

    @Test fun paycheckMonthly() {
        val (gross, tax, net) = Finance.paycheck(20.0, 40.0, 20.0)
        assertEquals(3466.67, gross, 0.01)
        assertEquals(gross * 0.2, tax, 1e-6)
        assertEquals(gross - tax, net, 1e-6)
    }

    @Test fun creditPayoffBasic() {
        val (months, interest, total) = Finance.creditPayoff(1000.0, 12.0, 100.0)
        assertTrue(months <= 12)
        assertTrue(interest > 0)
        assertTrue(total > 1000.0)
    }

    @Test fun profitMarginHalf() {
        assertEquals(50.0, Finance.profitMargin(50.0, 100.0), 1e-9)
    }

    @Test fun zakatBasic() {
        assertEquals(225.0, Finance.zakat(10000.0, 1000.0), 1e-9)
    }

    @Test fun zakatBelowNisab() {
        assertEquals(0.0, Finance.zakat(1000.0, 0.0, 5000.0), 1e-9)
    }

    @Test fun edgeCasesSafe() {
        assertEquals(0.0, Finance.emi(1000.0, 5.0, 0), 0.0)
        assertEquals(0.0, Finance.unitPrice(10.0, 0.0), 0.0)
        try {
            Finance.compound(100.0, 5.0, 1.0, 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.withTax(100.0, -100.0, true)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.rule72(0.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.sip(100.0, 5.0, 0.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun crashHardeningEdges() {
        assertEquals(0.0, Finance.emi(1000.0, 5.0, -3), 0.0)
        assertEquals(0.0, Finance.profitMargin(50.0, 0.0), 0.0)
        assertEquals(0.0, Finance.gpa(emptyList()), 0.0)
        assertEquals(Triple(0, 0.0, 0.0), Finance.creditPayoff(0.0, 12.0, 100.0))
        try {
            Finance.rentVsBuy(1500.0, 3.0, 300000.0, 20.0, 6.0, 101)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.rentVsBuy(1500.0, 3.0, 300000.0, 20.0, 6.0, 1e10)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.amortization(1000.0, 5.0, 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.amortization(1000.0, 5.0, 1201)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.gradeNeeded(80.0, 100.0, 90.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.creditPayoff(10000.0, 24.0, 1.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.cagr(0.0, 100.0, 5.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Finance.rule72(-5.0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun investRoiYearlyFifty() {
        val (profit, margin, annualized) = Finance.investRoi(1000.0, 1500.0, 365)
        assertEquals(500.0, profit, 1e-9)
        assertEquals(50.0, margin, 1e-9)
        assertEquals(50.0, annualized, 1e-9)
    }

    @Test fun gstForwardEighteen() {
        val (net, tax, gross) = Finance.gstForward(1000.0, 18.0, true)
        assertEquals(1000.0, net, 1e-9)
        assertEquals(180.0, tax, 1e-9)
        assertEquals(1180.0, gross, 1e-9)
    }

    @Test fun gstReverseEighteen() {
        val (net, tax, gross) = Finance.gstReverse(1180.0, 18.0)
        assertEquals(1000.0, net, 1e-9)
        assertEquals(180.0, tax, 1e-9)
        assertEquals(1180.0, gross, 1e-9)
    }

    @Test fun bmiDeltaOverweight() {
        assertEquals(13.74, calc.u.core.HealthPlus.bmiDelta(90.0, 175.0), 0.05)
    }

    @Test fun discountForwardTwenty() {
        val (final, savings) = Finance.discountForward(2000.0, 20.0)
        assertEquals(1600.0, final, 1e-9)
        assertEquals(400.0, savings, 1e-9)
    }

    @Test fun discountReverseTwenty() {
        val (pct, savings) = Finance.discountReverse(2000.0, 1600.0)
        assertEquals(20.0, pct, 1e-9)
        assertEquals(400.0, savings, 1e-9)
    }

    @Test fun tipRoundUpNoRound() {
        val (per, total, tip) = Finance.tipRoundUp(1000.0, 100.0, 2)
        assertEquals(550.0, per, 1e-9)
        assertEquals(1100.0, total, 1e-9)
        assertEquals(100.0, tip, 1e-9)
    }

    @Test fun investFreqMatchesCompound() {
        val (invested, maturity, gains) = Finance.investFreq(5000.0, 12.0, 10.0, 1)
        assertEquals(5000.0, invested, 1e-9)
        assertEquals(Finance.compound(5000.0, 12.0, 10.0, 1), maturity, 1e-6)
        assertEquals(maturity - invested, gains, 1e-6)
    }

    @Test fun daysToBirthdaySane() {
        val today = java.time.LocalDate.parse("2026-01-15")
        val days = Finance.daysToBirthday(3, 10, today.toEpochDay())
        assertTrue(days >= 0)
        assertEquals(java.time.temporal.ChronoUnit.DAYS.between(today, java.time.LocalDate.parse("2026-03-10")), days)
    }

    @Test fun dateOffsetNinetyCrossesMonth() {
        assertEquals("2026-04-15", Finance.dateOffset("2026-01-15", 90))
    }

    @Test fun tzUtcToIstanbulPlus3() {
        assertEquals("15:00", Finance.timezoneConvert("12:00", "UTC", "Europe/Istanbul"))
    }
}
