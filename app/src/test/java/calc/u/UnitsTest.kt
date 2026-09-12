package calc.u

import calc.u.core.Units
import org.junit.Assert.*
import org.junit.Test

class UnitsTest {
    @Test fun viscosityPasToCentipoise() {
        assertEquals(1000.0, Units.convert(1.0, Units.viscosity["Pa·s"]!!, Units.viscosity["cP"]!!), 1e-9)
        assertEquals(1.0, Units.convert(1000.0, Units.viscosity["cP"]!!, Units.viscosity["Pa·s"]!!), 1e-9)
    }

    @Test fun viscosityPoiseRoundTrip() {
        val base = Units.convert(1.0, Units.viscosity["P"]!!, Units.viscosity["Pa·s"]!!)
        assertEquals(0.1, base, 1e-12)
        assertEquals(1.0, Units.convert(base, Units.viscosity["Pa·s"]!!, Units.viscosity["P"]!!), 1e-9)
        val imp = Units.convert(1.0, Units.viscosity["lbf·s/ft²"]!!, Units.viscosity["Pa·s"]!!)
        assertEquals(47.880258, imp, 1e-6)
        assertEquals(1.0, Units.convert(imp, Units.viscosity["Pa·s"]!!, Units.viscosity["lbf·s/ft²"]!!), 1e-9)
    }

    @Test fun radiationSievertToRem() {
        assertEquals(100.0, Units.convert(1.0, Units.radiation["Sv"]!!, Units.radiation["rem"]!!), 1e-9)
        assertEquals(1.0, Units.convert(100.0, Units.radiation["rem"]!!, Units.radiation["Sv"]!!), 1e-9)
    }

    @Test fun radiationGrayToRad() {
        assertEquals(100.0, Units.convert(1.0, Units.radiation["Gy"]!!, Units.radiation["rad"]!!), 1e-9)
        val base = Units.convert(1.0, Units.radiation["mSv"]!!, Units.radiation["Sv"]!!)
        assertEquals(0.001, base, 1e-12)
        assertEquals(1.0, Units.convert(base, Units.radiation["Sv"]!!, Units.radiation["mSv"]!!), 1e-9)
    }

    @Test fun illuminanceFootCandle() {
        assertEquals(10.76391041671, Units.convert(1.0, Units.illuminance["fc"]!!, Units.illuminance["lux"]!!), 1e-6)
        val base = Units.convert(1.0, Units.illuminance["klx"]!!, Units.illuminance["lux"]!!)
        assertEquals(1000.0, base, 1e-9)
        assertEquals(1.0, Units.convert(base, Units.illuminance["lux"]!!, Units.illuminance["klx"]!!), 1e-9)
    }

    @Test fun magneticTeslaToGauss() {
        assertEquals(10000.0, Units.convert(1.0, Units.magnetic["T"]!!, Units.magnetic["G"]!!), 1e-9)
        assertEquals(1.0, Units.convert(10000.0, Units.magnetic["G"]!!, Units.magnetic["T"]!!), 1e-9)
    }

    @Test fun magneticMilliTeslaRoundTrip() {
        val base = Units.convert(1.0, Units.magnetic["mT"]!!, Units.magnetic["T"]!!)
        assertEquals(0.001, base, 1e-12)
        assertEquals(1.0, Units.convert(base, Units.magnetic["T"]!!, Units.magnetic["mT"]!!), 1e-9)
    }

    @Test fun densityGramPerCm3() {
        assertEquals(1000.0, Units.convert(1.0, Units.density["g/cm³"]!!, Units.density["kg/m³"]!!), 1e-9)
        assertEquals(1.0, Units.convert(1000.0, Units.density["kg/m³"]!!, Units.density["g/cm³"]!!), 1e-9)
    }

    @Test fun densityImperialRoundTrip() {
        val base = Units.convert(1.0, Units.density["lb/ft³"]!!, Units.density["kg/m³"]!!)
        assertEquals(16.01846337395, base, 1e-6)
        assertEquals(1.0, Units.convert(base, Units.density["kg/m³"]!!, Units.density["lb/ft³"]!!), 1e-9)
    }

    @Test fun specificEnergyRoundTrip() {
        assertEquals(4184.0, Units.convert(1.0, Units.specificenergy["kcal/kg"]!!, Units.specificenergy["J/kg"]!!), 1e-9)
        val base = Units.convert(1.0, Units.specificenergy["Wh/kg"]!!, Units.specificenergy["J/kg"]!!)
        assertEquals(3600.0, base, 1e-9)
        assertEquals(1.0, Units.convert(base, Units.specificenergy["J/kg"]!!, Units.specificenergy["Wh/kg"]!!), 1e-9)
    }

    @Test fun lengthLightYear() {
        assertEquals(9.4607e15, Units.convert(1.0, Units.length["ly"]!!, Units.length["m"]!!), 1e11)
        assertEquals(1852.0, Units.convert(1.0, Units.length["nmi"]!!, Units.length["m"]!!), 1e-9)
        assertEquals(1e-10, Units.convert(1.0, Units.length["Å"]!!, Units.length["m"]!!), 1e-20)
        val base = Units.convert(1.0, Units.length["pc"]!!, Units.length["m"]!!)
        assertEquals(3.08567758149137e16, base, 1e10)
        assertEquals(1.0, Units.convert(base, Units.length["m"]!!, Units.length["pc"]!!), 1e-9)
    }

    @Test fun lengthSurveyChainLink() {
        assertEquals(22.0, Units.convert(1.0, Units.length["chain"]!!, Units.length["yd"]!!), 1e-9)
        assertEquals(20.1168, Units.convert(1.0, Units.length["chain"]!!, Units.length["m"]!!), 1e-9)
        assertEquals(0.01, Units.convert(1.0, Units.length["link"]!!, Units.length["chain"]!!), 1e-12)
        assertEquals(1.0, Units.convert(20.1168, Units.length["m"]!!, Units.length["chain"]!!), 1e-9)
    }

    @Test fun lengthHandMilRodFathom() {
        assertEquals(4.0, Units.convert(1.0, Units.length["hand"]!!, Units.length["in"]!!), 1e-9)
        assertEquals(0.001, Units.convert(1.0, Units.length["mil"]!!, Units.length["in"]!!), 1e-12)
        assertEquals(5.5, Units.convert(1.0, Units.length["rod"]!!, Units.length["yd"]!!), 1e-9)
        assertEquals(6.0, Units.convert(1.0, Units.length["fathom"]!!, Units.length["ft"]!!), 1e-9)
        assertEquals(1.0, Units.convert(0.1016, Units.length["m"]!!, Units.length["hand"]!!), 1e-9)
    }

    @Test fun volumeQuartPintDramMetricTbsp() {
        assertEquals(0.946352946, Units.convert(1.0, Units.volume["qt"]!!, Units.volume["L"]!!), 1e-9)
        assertEquals(2.0, Units.convert(1.0, Units.volume["qt"]!!, Units.volume["pt"]!!), 1e-9)
        assertEquals(16.0, Units.convert(1.0, Units.volume["pt"]!!, Units.volume["fl-oz"]!!), 1e-4)
        assertEquals(0.125, Units.convert(1.0, Units.volume["fl_dram"]!!, Units.volume["fl-oz"]!!), 1e-9)
        assertEquals(15.0, Units.convert(1.0, Units.volume["tbsp_metric"]!!, Units.volume["mL"]!!), 1e-9)
    }

    @Test fun areaSectionTownshipRood() {
        assertEquals(2589988.110336, Units.convert(1.0, Units.area["section"]!!, Units.area["m2"]!!), 1e-6)
        assertEquals(36.0, Units.convert(1.0, Units.area["township"]!!, Units.area["section"]!!), 1e-9)
        assertEquals(0.25, Units.convert(1.0, Units.area["rood"]!!, Units.area["acre"]!!), 1e-9)
        val base = Units.convert(1.0, Units.area["township"]!!, Units.area["m2"]!!)
        assertEquals(1.0, Units.convert(base, Units.area["m2"]!!, Units.area["township"]!!), 1e-9)
    }

    @Test fun timeWeekFortnightYear() {
        assertEquals(7.0, Units.convert(1.0, Units.time["week"]!!, Units.time["day"]!!), 1e-9)
        assertEquals(14.0, Units.convert(1.0, Units.time["fortnight"]!!, Units.time["day"]!!), 1e-9)
        assertEquals(1209600.0, Units.convert(1.0, Units.time["fortnight"]!!, Units.time["s"]!!), 1e-6)
        assertEquals(365.242198781, Units.convert(1.0, Units.time["yr"]!!, Units.time["day"]!!), 1e-6)
    }

    @Test fun speedKnot() {
        assertEquals(1852.0 / 3600.0, Units.convert(1.0, Units.speed["knot"]!!, Units.speed["m/s"]!!), 1e-12)
        val mph = Units.convert(1.0, Units.speed["knot"]!!, Units.speed["mph"]!!)
        assertEquals(1.0, Units.convert(mph, Units.speed["mph"]!!, Units.speed["knot"]!!), 1e-9)
    }

    @Test fun pressureKsiInHgTorr() {
        assertEquals(1000.0, Units.convert(1.0, Units.pressure["ksi"]!!, Units.pressure["psi"]!!), 1e-9)
        assertEquals(3386.389, Units.convert(1.0, Units.pressure["inHg"]!!, Units.pressure["Pa"]!!), 1e-3)
        assertEquals(1.0, Units.convert(1.0, Units.pressure["torr"]!!, Units.pressure["mmHg"]!!), 1e-5)
    }

    @Test fun energyBtuThermTonTNT() {
        assertEquals(1055.05585262, Units.convert(1.0, Units.energy["Btu"]!!, Units.energy["J"]!!), 1e-6)
        assertEquals(1e5, Units.convert(1.0, Units.energy["therm"]!!, Units.energy["Btu"]!!), 1e-6)
        assertEquals(4.184e9, Units.convert(1.0, Units.energy["tonTNT"]!!, Units.energy["J"]!!), 1e3)
        assertEquals(1.0, Units.convert(4.184e9, Units.energy["J"]!!, Units.energy["tonTNT"]!!), 1e-9)
    }

    @Test fun printingPointPica() {
        assertEquals(25.4 / 72.0, Units.printing["point"]!!.toBase, 1e-12)
        assertEquals(12.0, Units.convert(1.0, Units.printing["pica"]!!, Units.printing["point"]!!), 1e-9)
        assertEquals(1.0, Units.convert(12.0, Units.printing["point"]!!, Units.printing["pica"]!!), 1e-9)
    }

    @Test fun fuelZeroThrows() {
        try {
            Units.convertFuel(0.0, "mpg_us", "l_100km")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Units.convertFuel(10.0, "bogus", "l_100km")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun convertZeroTargetThrows() {
        try {
            Units.convert(1.0, Units.length["m"]!!, Units.UnitDef("zero", 0.0))
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun megaLengthAu() {
        assertEquals(1.495978707e11, Units.convert(1.0, Units.length["au"]!!, Units.length["m"]!!), 1e5)
        assertEquals(42194.988, Units.convert(1.0, Units.length["marathon"]!!, Units.length["m"]!!), 1e-6)
        assertEquals(22.0, Units.convert(1.0, Units.length["chain"]!!, Units.length["yd"]!!), 1e-9)
    }

    @Test fun megaMassAmu() {
        assertEquals(1.660539066605e-27, Units.convert(1.0, Units.mass["amu"]!!, Units.mass["kg"]!!), 1e-33)
        assertEquals(2e-4, Units.convert(1.0, Units.mass["carat"]!!, Units.mass["kg"]!!), 1e-12)
        assertEquals(907.18474, Units.convert(1.0, Units.mass["ton_us"]!!, Units.mass["kg"]!!), 1e-6)
    }

    @Test fun megaTimeMonth() {
        assertEquals(2629743.8312232, Units.convert(1.0, Units.time["month"]!!, Units.time["s"]!!), 1e-3)
        assertEquals(3.15569259746784e9, Units.convert(1.0, Units.time["century"]!!, Units.time["s"]!!), 1e3)
    }

    @Test fun megaTempRankine() {
        assertEquals(273.15, Units.convertTemp(0.0, "C", "K"), 1e-9)
        assertEquals(491.67, Units.convertTemp(0.0, "C", "R"), 1e-9)
        assertEquals(459.67, Units.convertTemp(0.0, "F", "R"), 1e-9)
    }

    @Test fun megaAreaVolume() {
        assertEquals(1e-28, Units.convert(1.0, Units.area["barn"]!!, Units.area["m2"]!!), 1e-38)
        assertEquals(1600.0, Units.convert(1.0, Units.area["rai"]!!, Units.area["m2"]!!), 1e-9)
        assertEquals(158.987294928, Units.convert(1.0, Units.volume["barrel"]!!, Units.volume["L"]!!), 1e-6)
        assertEquals(0.75, Units.convert(1.0, Units.volume["winebottle"]!!, Units.volume["L"]!!), 1e-9)
    }

    @Test fun megaDataQuantityConcentration() {
        assertEquals(1024.0, Units.convert(1.0, Units.data["KiB"]!!, Units.data["B"]!!), 1e-9)
        assertEquals(0.125, Units.convert(1.0, Units.data["bit"]!!, Units.data["B"]!!), 1e-12)
        assertEquals(1e-6, Units.convert(1.0, Units.concentration["ppm"]!!, Units.concentration["fraction"]!!), 1e-12)
        assertEquals(12.0, Units.convert(1.0, Units.quantity["dozen"]!!, Units.quantity["count"]!!), 1e-9)
    }

    @Test fun megaEnergyRadioactivityRotation() {
        assertEquals(1.602176634e-19, Units.convert(1.0, Units.energy["eV"]!!, Units.energy["J"]!!), 1e-28)
        assertEquals(3.7e10, Units.convert(1.0, Units.radioactivity["Ci"]!!, Units.radioactivity["Bq"]!!), 1e3)
        assertEquals(0.10471975511965977, Units.convert(1.0, Units.rotational["rpm"]!!, Units.rotational["rad/s"]!!), 1e-12)
        assertEquals(90.0, Units.convert(1.0, Units.angle["quadrant"]!!, Units.angle["deg"]!!), 1e-9)
        assertEquals(0.01, Units.convert(1.0, Units.radiation["cGy"]!!, Units.radiation["Gy"]!!), 1e-12)
    }

    @Test fun megaElectricalFrequency() {
        assertEquals(1000.0, Units.convert(1.0, Units.potential["kV"]!!, Units.potential["V"]!!), 1e-9)
        assertEquals(3600.0, Units.convert(1.0, Units.charge["Ah"]!!, Units.charge["C"]!!), 1e-9)
        assertEquals(1e9, Units.convert(1.0, Units.frequency["GHz"]!!, Units.frequency["Hz"]!!), 1e0)
        assertEquals(12.566370614359172, Units.convert(1.0, Units.solidangle["sphere"]!!, Units.solidangle["sr"]!!), 1e-9)
    }

    @Test fun crashHardeningEdges() {
        assertEquals(32.0, Units.convertTemp(0.0, "C", "F"), 1e-9)
        try {
            Units.convertTemp(0.0, "X", "C")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Units.convertTemp(0.0, "C", "X")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Units.convertTemp(0.0, "", "")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Units.fromBase(10.0, 1)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Units.fromBase(10.0, 37)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals("0", Units.fromBase(0.0, 16))
        assertEquals("-101", Units.fromBase(-5.0, 2))
        assertEquals("Error", Units.fromBase(Double.NaN, 10))
        assertEquals("1000000000000000000", Units.fromBase(1e18, 10))
        assertEquals("—", Units.toRoman(-5))
        assertEquals("—", Units.toRoman(Int.MAX_VALUE))
        try {
            Units.convertFuel(10.0, "mpg_us", "bogus")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals(-5.0, Units.convertFuel(-5.0, "l_100km", "l_100km"), 0.0)
    }
}
