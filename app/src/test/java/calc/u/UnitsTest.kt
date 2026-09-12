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

    @Test fun massGrainStoneSlug() {
        assertEquals(6.479891e-5, Units.convert(1.0, Units.mass["gr"]!!, Units.mass["kg"]!!), 1e-12)
        assertEquals(6.35029318, Units.convert(1.0, Units.mass["st"]!!, Units.mass["kg"]!!), 1e-9)
        assertEquals(14.59390294, Units.convert(1.0, Units.mass["slug"]!!, Units.mass["kg"]!!), 1e-6)
        val base = Units.convert(1.0, Units.mass["st"]!!, Units.mass["kg"]!!)
        assertEquals(1.0, Units.convert(base, Units.mass["kg"]!!, Units.mass["st"]!!), 1e-9)
    }
}
