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
}
