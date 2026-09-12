package calc.u

import calc.u.core.Finance
import org.junit.Assert.*
import org.junit.Test

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
}
