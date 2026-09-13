package calc.u

import calc.u.core.ClockKit
import calc.u.core.ColorKit
import calc.u.core.Currency
import calc.u.core.Engine
import calc.u.core.Finance
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.ScreenKit
import calc.u.core.Units
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class EngineTest {
    @Test fun basic() {
        assertEquals("4", Engine.format(Engine.eval("2+2").getOrThrow()))
    }
    @Test fun sci() {
        assertNotNull(Engine.eval("sin(30)", true).getOrNull())
    }
    @Test fun fraction() {
        assertEquals(Pair(1L, 3L), Engine.toFraction(0.3333333, 100))
    }
    @Test fun prime() { assertTrue(Engine.isPrime(13)); assertFalse(Engine.isPrime(15)) }
    @Test fun quadratic() { assertEquals(2, Engine.solveQuadratic(1.0, -3.0, 2.0).size) }
    @Test fun units() {
        assertEquals(1000.0, Units.convert(1.0, Units.length["km"]!!, Units.length["m"]!!), 1e-9)
        assertEquals(32.0, Units.convertTemp(0.0, "C", "F"), 1e-9)
    }
    @Test fun finance() {
        assertEquals(1000.0 / 12, Finance.emi(1000.0, 0.0, 12), 1e-9)
        assertTrue(Finance.compound(100.0, 10.0, 1.0) > 100)
    }
    @Test fun geometryHealth() {
        assertEquals(78.5, Geometry.circleArea(5.0), 0.1)
        assertTrue(HealthDate.bmi(70.0, 175.0) > 20)
    }
    @Test fun currencyCodes() { assertTrue(Currency.codes.size >= 60) }

    @Test fun formatHonorsScaleAndNonFinite() {
        assertEquals("3.14", Engine.format(BigDecimal("3.14000")))
        assertEquals("3.142", Engine.format(BigDecimal("3.14159"), 3))
        assertEquals("Error", Engine.format(BigDecimal("1E+100000"), 10))
    }
    @Test fun radRewriteKeepsAsin() {
        val r = Engine.eval("ASIN(1)", false).getOrThrow().toDouble()
        assertEquals(90.0, r, 1e-9)
        val s = Engine.eval("SIN(PI/2)", false).getOrThrow().toDouble()
        assertEquals(1.0, s, 1e-6)
    }
    @Test fun complexRoots() {
        val roots = Engine.solveQuadratic(1.0, -2.0, 5.0)
        assertEquals(1, roots.size)
        assertTrue(roots[0].contains("±") && roots[0].contains("i"))
    }
    @Test fun linearSystem() {
        val (x, y) = Engine.solveLinearSystem2x2(2.0, 3.0, 8.0, 1.0, -1.0, 1.0)
        assertEquals(2.2, x.toDouble(), 1e-9)
        assertEquals(1.2, y.toDouble(), 1e-9)
    }
    @Test fun factorialMeans() {
        assertEquals(120L, Engine.factorial(5))
        assertEquals(2.5, Engine.mean(listOf(1.0, 2.0, 3.0, 4.0)), 1e-9)
        assertEquals(1.92, Engine.mean(listOf(1.0, 2.0, 3.0, 4.0), "harmonic"), 0.01)
        assertEquals(2.21, Engine.mean(listOf(1.0, 2.0, 3.0, 4.0), "geometric"), 0.01)
    }
    @Test fun amortizationZeroAndSchedule() {
        val zero = Finance.amortization(1200.0, 0.0, 12)
        assertEquals(12, zero.size)
        assertEquals(100.0, zero.first().second, 1e-9)
        assertEquals(0.0, zero.first().third, 1e-9)
        val sched = Finance.amortization(10000.0, 12.0, 12)
        assertEquals(12, sched.size)
        assertTrue(sched.first().third > sched.last().third)
        assertEquals(10000.0, sched.sumOf { it.second }, 50.0)
    }
    @Test fun fuelConversions() {
        assertEquals(235.214583 / 30.0, Units.convertFuel(30.0, "mpg_us", "l_100km"), 1e-6)
        assertEquals(30.0, Units.convertFuel(235.214583 / 30.0, "l_100km", "mpg_us"), 1e-6)
        assertEquals(20.0, Units.convertFuel(5.0, "l_100km", "km_l"), 1e-9)
        assertEquals(5.0, Units.convertFuel(20.0, "km_l", "l_100km"), 1e-9)
    }
    @Test fun romanBoundsAndFractions() {
        assertEquals("—", Units.toRoman(0))
        assertEquals("—", Units.toRoman(4000))
        assertEquals("MCMXCIV", Units.toRoman(1994))
        assertEquals("1010.1", Units.fromBase(10.5, 2))
    }
    @Test fun combinatoricsOverflowSafe() {
        assertEquals(10L, Engine.nCr(5, 2))
        assertEquals(20L, Engine.nPr(5, 2))
        assertEquals(1L, Engine.factorial(0))
    }
    @Test fun currencyDedupeAndRate() {
        assertFalse(Currency.codes.contains("HRK"))
        assertEquals(1, Currency.codes.count { it == "KWD" })
        assertEquals(1, Currency.codes.count { it == "QAR" })
        val usd = Currency.Rate("USD", 1.0)
        val eur = Currency.Rate("EUR", 0.92)
        assertEquals(92.0, Currency.convert(100.0, usd, eur), 1e-9)
    }
    @Test fun cookingCupIs236588Ml() {
        assertEquals(236.588, Units.convert(1.0, Units.cooking["cup"]!!, Units.cooking["ml"]!!), 0.01)
        assertEquals(128.0, Units.convertCookingToWeight(236.5882365, 128.0), 0.01)
    }
    @Test fun shoeEu42Is27cm() {
        assertEquals(27.0, Units.convert(42.0, Units.shoe["EU"]!!, Units.shoe["CM"]!!), 0.5)
    }
    @Test fun romanBoundsHold() {
        assertEquals("—", Units.toRoman(0))
        assertEquals("—", Units.toRoman(4000))
    }
    @Test fun historicFurlongIs201m() {
        assertEquals(201.168, Units.convert(1.0, Units.historic["furlong"]!!, Units.length["m"]!!), 1e-6)
    }
    @Test fun currencyConvertMath() {
        try {
            Currency.convert(100.0, 0.0, 0.92)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals(100.0, Currency.convert(100.0, 1.0, 1.0), 1e-9)
        assertEquals(50.0, Currency.convert(100.0, 2.0, 1.0), 1e-9)
        assertEquals(184.0, Currency.convert(100.0, 0.5, 0.92), 1e-9)
    }
    @Test fun cookingRoundTrip() {
        val ml = Units.convert(2.0, Units.cooking["cup"]!!, Units.cooking["ml"]!!)
        assertEquals(2.0, Units.convert(ml, Units.cooking["ml"]!!, Units.cooking["cup"]!!), 1e-9)
        val l = Units.convert(1.0, Units.cooking["gallon_us"]!!, Units.cooking["l"]!!)
        assertEquals(1.0, Units.convert(l, Units.cooking["l"]!!, Units.cooking["gallon_us"]!!), 1e-9)
    }
    @Test fun shoeRoundTrip() {
        val cm = Units.convert(9.0, Units.shoe["US_M"]!!, Units.shoe["CM"]!!)
        assertEquals(9.0, Units.convert(cm, Units.shoe["CM"]!!, Units.shoe["US_M"]!!), 1e-9)
    }
    @Test fun ringRoundTrip() {
        val eu = Units.convert(10.0, Units.ring["US"]!!, Units.ring["EU"]!!)
        assertEquals(10.0, Units.convert(eu, Units.ring["EU"]!!, Units.ring["US"]!!), 1e-9)
    }
    @Test fun angleDegToRad() {
        assertEquals(2 * Math.PI, Units.convert(360.0, Units.angle["deg"]!!, Units.angle["rad"]!!), 1e-9)
    }
    @Test fun angleTurnToDeg() {
        assertEquals(360.0, Units.convert(1.0, Units.angle["turn"]!!, Units.angle["deg"]!!), 1e-9)
    }
    @Test fun forceLbfToNewton() {
        assertEquals(4.44822, Units.convert(1.0, Units.force["lbf"]!!, Units.force["N"]!!), 1e-5)
    }
    @Test fun accelerationGToBase() {
        assertEquals(9.80665, Units.convert(1.0, Units.acceleration["g"]!!, Units.acceleration["m/s²"]!!), 1e-9)
    }
    @Test fun newTools() {
        val m = calc.u.core.Matrix.of2x2(4.0, 7.0, 2.0, 6.0)
        assertEquals(10.0, m.determinant(), 1e-9)
        val t = m.transpose()
        assertEquals(7.0, t[1, 0], 1e-9)
        val inv = m.inverse()
        val id = m.multiply(inv)
        assertEquals(1.0, id[0, 0], 1e-9)
        assertEquals(0.0, id[0, 1], 1e-9)
        assertEquals(0.0, id[1, 0], 1e-9)
        assertEquals(1.0, id[1, 1], 1e-9)
        try {
            calc.u.core.Matrix.of2x2(1.0, 2.0, 2.0, 4.0).inverse()
            fail("singular should throw")
        } catch (e: IllegalArgumentException) { }
        val roots = Engine.solveCubic(1.0, -6.0, 11.0, -6.0).mapNotNull { it.toDoubleOrNull() }.sorted()
        assertEquals(listOf(1.0, 2.0, 3.0), roots.map { Math.round(it).toDouble() })
        assertEquals(3.0, Engine.statsMedian(listOf(1.0, 2.0, 3.0, 4.0, 5.0)), 1e-9)
        assertEquals(2.0, Engine.statsMode(listOf(1.0, 2.0, 2.0, 3.0)), 1e-9)
        assertEquals(2.0, Engine.statsVariance(listOf(1.0, 2.0, 3.0, 4.0, 5.0)), 1e-9)
        assertEquals(Math.sqrt(2.0), Engine.statsStdev(listOf(1.0, 2.0, 3.0, 4.0, 5.0)), 1e-9)
        assertEquals(6.0, Engine.derivative("x^2", 3.0, true), 1e-3)
        assertEquals(0.5, Engine.integral("x", 0.0, 1.0, true), 1e-6)
        assertTrue(calc.u.core.Constants.search("Planck").isNotEmpty())
        assertTrue(calc.u.core.Constants.search("planck").any { it.name.contains("Planck") })
    }
    @Test fun torqueFlowDatarateRoundTrip() {
        val base = Units.convert(1.0, Units.torque["lbf·ft"]!!, Units.torque["N·m"]!!)
        assertEquals(1.0, Units.convert(base, Units.torque["N·m"]!!, Units.torque["lbf·ft"]!!), 1e-9)
        val si = Units.convert(1.0, Units.flow["L/min"]!!, Units.flow["m³/s"]!!)
        assertEquals(1.0, Units.convert(si, Units.flow["m³/s"]!!, Units.flow["L/min"]!!), 1e-9)
        val bps = Units.convert(1.0, Units.datarate["MBps"]!!, Units.datarate["bps"]!!)
        assertEquals(1.0, Units.convert(bps, Units.datarate["bps"]!!, Units.datarate["MBps"]!!), 1e-9)
    }
    @Test fun stolenSlice() {
        assertEquals(Triple(255, 0, 0), ColorKit.hexToRgb("#FF0000"))
        assertEquals("16:9", ScreenKit.aspectRatio(1920, 1080))
        assertTrue(ScreenKit.ppi(1920, 1080, 6.1) > 0)
        val tri = Geometry.solveTriangleSSS(3.0, 4.0, 5.0)
        assertEquals(36.87, tri["angleA"]!!, 0.01)
        assertEquals(53.13, tri["angleB"]!!, 0.01)
        assertEquals(90.0, tri["angleC"]!!, 0.01)
        assertEquals("Friday", ClockKit.weekdayName(2026, 9, 11))
    }
    @Test fun solve3x3Identity() {
        val id = listOf(listOf(1.0, 0.0, 0.0), listOf(0.0, 1.0, 0.0), listOf(0.0, 0.0, 1.0))
        assertEquals(listOf("1", "2", "3"), Engine.solve3x3(id, listOf(1.0, 2.0, 3.0)))
    }
    @Test fun vectorDot() {
        assertEquals(32.0, calc.u.core.VectorKit.dot(listOf(1.0, 2.0, 3.0), listOf(4.0, 5.0, 6.0)), 1e-9)
    }
    @Test fun vectorCross() {
        assertEquals(listOf(0.0, 0.0, 1.0), calc.u.core.VectorKit.cross(listOf(1.0, 0.0, 0.0), listOf(0.0, 1.0, 0.0)))
    }
    @Test fun clockAngle3() {
        assertEquals(90.0, calc.u.core.ClockAngle.angle(3, 0), 1e-9)
    }

    @Test fun edgeCasesNoCrash() {
        assertNull(Engine.toFraction(Double.NaN, 100))
        assertNull(Engine.toFraction(1.5, 0))
        assertNull(Engine.toFraction(Double.POSITIVE_INFINITY, 100))
        try {
            Engine.mean(listOf(1.0), "bogus")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun emptyListThrowsIAE() {
        try {
            Engine.mean(emptyList())
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.statsMedian(emptyList())
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.statsMode(emptyList())
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.statsVariance(emptyList())
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun hugeCombinatoricsThrowIAE() {
        try {
            Engine.nCr(100001L, 2L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.nPr(100001L, 2L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.factorial(-1L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.numberToWords(-1L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun singular3x3NoUniqueSolution() {
        val singular = listOf(listOf(1.0, 2.0, 3.0), listOf(1.0, 2.0, 3.0), listOf(4.0, 5.0, 6.0))
        assertEquals(listOf("no unique solution"), Engine.solve3x3(singular, listOf(1.0, 2.0, 3.0)))
        try {
            Engine.solve3x3(listOf(listOf(1.0)), listOf(1.0))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun crashHardeningEdges() {
        assertTrue(Engine.eval("").isFailure)
        assertTrue(Engine.eval("   ").isFailure)
        assertTrue(Engine.eval("(").isFailure)
        assertTrue(Engine.eval("2+").isSuccess)
        assertTrue(Engine.eval("A".repeat(20001)).isFailure)
        try {
            Engine.mean(listOf(0.0, 1.0), "geometric")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.mean(listOf(1.0, 0.0), "harmonic")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.mean(listOf(-1.0, 2.0), "g")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Engine.nCr(100L, 50L)
            fail("expected AE")
        } catch (e: ArithmeticException) { }
        try {
            Engine.factorial(30L)
            fail("expected AE")
        } catch (e: ArithmeticException) { }
        assertNull(Engine.toFraction(1e308, 1000))
        try {
            Engine.solve3x3(
                listOf(listOf(Double.NaN, 0.0, 0.0), listOf(0.0, 1.0, 0.0), listOf(0.0, 0.0, 1.0)),
                listOf(1.0, 2.0, 3.0)
            )
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals(0L, Engine.gcd(0L, 0L))
        assertEquals(0L, Engine.lcm(0L, 5L))
        assertEquals(0.0, calc.u.core.VectorKit.dot(emptyList(), emptyList()), 0.0)
    }

    @Test fun validateEmpty() {
        assertEquals("Empty", Engine.validateExpr(""))
        assertEquals("Empty", Engine.validateExpr("   "))
    }

    @Test fun validateUnbalanced() {
        assertNull(Engine.validateExpr("(2+3"))
        assertEquals("Unbalanced brackets", Engine.validateExpr("2+3)"))
    }

    @Test fun validateBadChar() {
        assertEquals("Invalid character", Engine.validateExpr("2+3\$"))
        assertEquals("Invalid character", Engine.validateExpr("2@3"))
    }

    @Test fun validateEmptyBrackets() {
        assertEquals("Empty brackets", Engine.validateExpr("2+()"))
    }

    @Test fun validateFactorialRange() {
        assertEquals("Too large", Engine.validateExpr("171!"))
        assertNull(Engine.validateExpr("5!"))
        assertNull(Engine.validateExpr("170!"))
    }

    @Test fun validateValidPasses() {
        assertNull(Engine.validateExpr("2+3"))
        assertNull(Engine.validateExpr("sin(30)"))
        assertNull(Engine.validateExpr("2+"))
        assertNull(Engine.validateExpr("5/0"))
    }

    @Test fun formatLocaleAwareGroupingAndRoundTrip() {
        val symbols = java.text.DecimalFormatSymbols.getInstance()
        val out = Engine.format(BigDecimal("1000.5"))
        assertTrue(out.contains(symbols.groupingSeparator) || out.contains(symbols.decimalSeparator))
        val parsed = java.text.NumberFormat.getInstance().parse(out)
        assertNotNull(parsed)
        assertEquals(1000.5, parsed!!.toDouble(), 1e-9)
    }

    @Test fun formatUsesDeviceLocaleSeparators() {
        val prev = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale("tr", "TR"))
            assertEquals("1.234,56", Engine.format(BigDecimal("1234.56")))
        } finally {
            java.util.Locale.setDefault(prev)
        }
        val out = Engine.format(BigDecimal("1234.56"))
        assertEquals(1234.56, java.text.NumberFormat.getInstance().parse(out)!!.toDouble(), 1e-9)
    }

    @Test fun repeatingRadixPercent() {
        assertEquals("0.(3)", Engine.repeatingToDecimal(1L, 3L))
        assertEquals("1/3", Engine.decimalToFraction("0.(3)"))
        assertEquals("255", Engine.radixConvert("FF", "", 16, 10, 12))
        assertTrue(Engine.radixConvert("0", "1", 10, 2, 12).startsWith("0.00011"))
        assertEquals("50%", Engine.formatPercentMode(0.5, "percent"))
    }

    @Test fun factorialActuallyEvaluates() {
        assertEquals(120.0, Engine.eval("5!").getOrThrow().toDouble(), 1e-9)
        assertEquals(5040.0, Engine.eval("7!+0").getOrThrow().toDouble(), 1e-9)
        assertEquals(3628800.0, Engine.eval("10!").getOrThrow().toDouble(), 1e-9)
    }

    @Test fun percentActuallyEvaluatesAsFraction() {
        assertEquals(0.5, Engine.eval("50%").getOrThrow().toDouble(), 1e-9)
        assertEquals(0.25, Engine.eval("25%").getOrThrow().toDouble(), 1e-9)
        assertEquals(100.1, Engine.eval("100+10%").getOrThrow().toDouble(), 1e-9)
    }

    @Test fun naturalLogActuallyEvaluates() {
        assertEquals(kotlin.math.ln(2.0), Engine.eval("ln(2)").getOrThrow().toDouble(), 1e-9)
        assertEquals(kotlin.math.ln(10.0), Engine.eval("ln(10)").getOrThrow().toDouble(), 1e-9)
    }

    @Test fun assignmentParsesNameAndEvaluatesRhs() {
        val a = Engine.parseAssignment("radius = 3 + 2")!!
        assertEquals("radius", a.name)
        assertEquals(5.0, a.value.toDouble(), 1e-9)
    }

    @Test fun assignmentRejectsReservedAndMalformed() {
        assertNull(Engine.parseAssignment("PI = 3.1"))
        assertNull(Engine.parseAssignment("sqrt = 2"))
        assertNull(Engine.parseAssignment("x = "))
        assertNull(Engine.parseAssignment("hello world"))
        assertNull(Engine.parseAssignment("1x = 3"))
    }

    @Test fun variablesSubstituteIntoExpression() {
        val vars = mapOf("radius" to java.math.BigDecimal("2"))
        assertEquals(12.566370614359172, Engine.eval("PI*radius^2", vars = vars).getOrThrow().toDouble(), 1e-9)
        assertEquals(3.0, Engine.eval("radius+1", vars = vars).getOrThrow().toDouble(), 1e-9)
    }

    @Test fun variablesDoNotClobberReservedOrIdentifiers() {
        val vars = mapOf("ln" to java.math.BigDecimal("5"))
        assertEquals(kotlin.math.ln(2.0), Engine.eval("ln(2)", vars = vars).getOrThrow().toDouble(), 1e-9)
        assertNull(Engine.parseAssignment("ln = 5"))
    }

    @Test fun unresolvedVariableIsNotSubstituted() {
        val vars = mapOf("x" to java.math.BigDecimal("7"))
        assertTrue(Engine.eval("y+1", vars = vars).isFailure)
    }

    @Test fun assignmentValueSurvivesRationalization() {
        val a = Engine.parseAssignment("kick = 0.362363")!!
        assertEquals("kick", a.name)
    }
}
