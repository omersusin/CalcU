package calc.u

import calc.u.core.Currency
import calc.u.core.ClockKit
import calc.u.core.Finance
import calc.u.core.HealthDate
import calc.u.core.HealthPlus
import calc.u.core.IdealWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BountyMoneyTest {
    private fun expectIAE(block: () -> Unit): String {
        try {
            block()
        } catch (e: IllegalArgumentException) {
            return e.message ?: ""
        }
        fail("expected IllegalArgumentException")
        return ""
    }

    @Test fun currencyConvertZeroRateThrows() {
        val msg = expectIAE { Currency.convert(100.0, 0.0, 0.92) }
        assertTrue(msg.isNotBlank())
    }

    @Test fun currencyConvertValue() {
        assertEquals(92.0, Currency.convert(100.0, 1.0, 0.92), 1e-9)
    }

    @Test fun emiZeroMonthsThrows() {
        assertTrue(expectIAE { Finance.emi(1000.0, 5.0, 0) }.isNotBlank())
        assertTrue(expectIAE { Finance.emi(1000.0, 5.0, -3) }.isNotBlank())
    }

    @Test fun emiValue() {
        assertEquals(888.49, Finance.emi(10000.0, 12.0, 12), 0.01)
    }

    @Test fun unitPriceZeroQtyThrows() {
        assertTrue(expectIAE { Finance.unitPrice(10.0, 0.0) }.isNotBlank())
    }

    @Test fun unitPriceValue() {
        assertEquals(0.02, Finance.unitPrice(10.0, 500.0), 1e-9)
    }

    @Test fun profitMarginZeroPriceThrows() {
        assertTrue(expectIAE { Finance.profitMargin(50.0, 0.0) }.isNotBlank())
        assertEquals(50.0, Finance.profitMargin(50.0, 100.0), 1e-9)
    }

    @Test fun gpaZeroCreditsThrows() {
        assertTrue(expectIAE { Finance.gpa(emptyList()) }.isNotBlank())
        assertEquals(3.5, Finance.gpa(listOf(4.0 to 3.0, 3.0 to 3.0)), 1e-9)
    }

    @Test fun targetHrAlwaysPercent() {
        assertEquals(133.0, HealthPlus.targetHeartRate(30, 70.0), 1e-9)
        assertEquals(1.33, HealthPlus.targetHeartRate(30, 0.7), 1e-9)
    }

    @Test fun waterExerciseDose() {
        assertEquals(2450.0, HealthPlus.waterIntakeMl(70.0, 0.0), 1e-9)
        assertEquals(2570.0, HealthPlus.waterIntakeMl(70.0, 30.0), 1e-9)
    }

    @Test fun depreciationDBChargeSemantics() {
        assertEquals(2000.0, Finance.depreciationDB(10000.0, 20.0, 1), 1e-6)
        assertEquals(1600.0, Finance.depreciationDB(10000.0, 20.0, 2), 1e-6)
        assertTrue(expectIAE { Finance.depreciationDB(10000.0, 20.0, 0) }.isNotBlank())
    }

    @Test fun depreciationDBBookHelper() {
        assertEquals(8000.0, Finance.depreciationDBBook(10000.0, 20.0, 1), 1e-6)
        assertEquals(6400.0, Finance.depreciationDBBook(10000.0, 20.0, 2), 1e-6)
    }

    @Test fun devineRobinsonClamped() {
        assertEquals(0.0, IdealWeight.devine(50.0, true), 0.0)
        assertEquals(0.0, IdealWeight.robinson(50.0, false), 0.0)
        assertEquals(70.57, IdealWeight.devine(175.0, true), 0.01)
        assertEquals(68.91, IdealWeight.robinson(175.0, true), 0.01)
    }

    @Test fun bmiRequiresPositive() {
        assertTrue(expectIAE { HealthDate.bmi(0.0, 175.0) }.isNotBlank())
        assertTrue(expectIAE { HealthDate.bmi(70.0, 0.0) }.isNotBlank())
        assertEquals(22.857, HealthDate.bmi(70.0, 175.0), 0.001)
    }

    @Test fun bodyFatRequiresValidGeometry() {
        assertTrue(expectIAE { HealthDate.bodyFatNavy(80.0, 90.0, 175.0, 0.0, true) }.isNotBlank())
        assertTrue(expectIAE { HealthDate.bodyFatNavy(0.0, 38.0, 175.0, 95.0, false) }.isNotBlank())
        assertEquals(16.94, HealthDate.bodyFatNavy(85.0, 38.0, 175.0, 95.0, true), 0.05)
    }

    @Test fun tdeeRequiresValidInputs() {
        assertTrue(expectIAE { HealthDate.tdee(0.0, 175.0, 30, true, 1.55) }.isNotBlank())
        assertTrue(expectIAE { HealthDate.tdee(70.0, 175.0, 200, true, 1.55) }.isNotBlank())
        assertTrue(expectIAE { HealthDate.tdee(70.0, 175.0, 30, true, 0.0) }.isNotBlank())
        assertEquals(2555.56, HealthDate.tdee(70.0, 175.0, 30, true, 1.55), 0.01)
    }

    @Test fun ohmNeedsTwoInputs() {
        assertTrue(expectIAE { HealthDate.ohm(null, null, null) }.isNotBlank())
        assertTrue(expectIAE { HealthDate.ohm(12.0, null, null) }.isNotBlank())
        assertEquals(Triple(6.0, 2.0, 3.0), HealthDate.ohm(null, 2.0, 3.0))
    }

    @Test fun ohmDivideByZeroThrows() {
        assertTrue(expectIAE { HealthDate.ohm(12.0, null, 0.0) }.isNotBlank())
        assertTrue(expectIAE { HealthDate.ohm(12.0, 0.0, null) }.isNotBlank())
    }

    @Test fun worldTimeUnknownZoneThrows() {
        val msg = expectIAE { ClockKit.worldTime("Not_A_Zone") }
        assertTrue(msg.contains("unknown zone"))
        assertTrue(ClockKit.worldTime("UTC").matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test fun zakatRequiresNisab() {
        assertTrue(expectIAE { Finance.zakat(10000.0, 1000.0) }.isNotBlank())
        assertTrue(expectIAE { Finance.zakat(10000.0, 1000.0, 0.0) }.isNotBlank())
        assertEquals(0.0, Finance.zakat(1000.0, 0.0, 5000.0), 1e-9)
        assertEquals(225.0, Finance.zakat(10000.0, 1000.0, 5000.0), 1e-9)
    }

    @Test fun paycheckTaxCap() {
        assertTrue(expectIAE { Finance.paycheck(20.0, 40.0, 101.0) }.isNotBlank())
        val (gross, tax, net) = Finance.paycheck(20.0, 40.0, 20.0)
        assertEquals(3466.67, gross, 0.01)
        assertEquals(gross * 0.2, tax, 1e-6)
        assertEquals(gross - tax, net, 1e-6)
    }

    @Test fun rentVsBuyDownPctCapAndRounding() {
        assertTrue(expectIAE { Finance.rentVsBuy(1500.0, 3.0, 300000.0, 101.0, 6.0, 10) }.isNotBlank())
        assertEquals(
            Finance.rentVsBuy(1500.0, 3.0, 300000.0, 20.0, 6.0, 11),
            Finance.rentVsBuy(1500.0, 3.0, 300000.0, 20.0, 6.0, 10.6)
        )
        assertEquals(
            Finance.rentVsBuy(1500.0, 3.0, 300000.0, 20.0, 6.0, 10),
            Finance.rentVsBuy(1500.0, 3.0, 300000.0, 20.0, 6.0, 10.4)
        )
    }

    @Test fun creditPayoffBelowInterestThrows() {
        val msg = expectIAE { Finance.creditPayoff(10000.0, 24.0, 1.0) }
        assertTrue(msg.contains("monthlyPayment"))
        val (months, interest, total) = Finance.creditPayoff(1000.0, 12.0, 100.0)
        assertTrue(months in 1..12)
        assertTrue(interest > 0)
        assertTrue(total > 1000.0)
    }

    @Test fun investTripleOrderDocumented() {
        val (invested, maturity, gains) = Finance.investFreq(5000.0, 12.0, 10.0, 1)
        assertEquals(5000.0, invested, 1e-9)
        assertEquals(Finance.compound(5000.0, 12.0, 10.0, 1), maturity, 1e-6)
        assertEquals(maturity - invested, gains, 1e-6)
        val (profit, margin, annualized) = Finance.investRoi(1000.0, 1500.0, 365)
        assertEquals(500.0, profit, 1e-9)
        assertEquals(50.0, margin, 1e-9)
        assertEquals(50.0, annualized, 1e-9)
        assertEquals(
            Finance.investRoi(1000.0, 1500.0, 366),
            Finance.investRoi(1000.0, 1500.0, 365.6)
        )
    }
}
