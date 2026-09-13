package calc.u

import calc.u.core.Units
import org.junit.Assert.*
import org.junit.Test

class P2UnitsTest {
    @Test fun glucoseFactorBothWays() {
        assertEquals(1.0, Units.convert(18.0182, Units.glucose.getValue("mg/dL"), Units.glucose.getValue("mmol/L")), 1e-9)
        assertEquals(18.0182, Units.convert(1.0, Units.glucose.getValue("mmol/L"), Units.glucose.getValue("mg/dL")), 1e-9)
    }

    @Test fun troyMassExact() {
        assertEquals(31.1034768, Units.convert(1.0, Units.mass.getValue("ozt"), Units.mass.getValue("g")), 1e-9)
        assertEquals(0.3732417216, Units.convert(1.0, Units.mass.getValue("lbt"), Units.mass.getValue("kg")), 1e-12)
        assertEquals(12.0, Units.convert(1.0, Units.mass.getValue("lbt"), Units.mass.getValue("ozt")), 1e-9)
    }

    @Test fun tempReaumur() {
        assertEquals(100.0, Units.convertTemp(80.0, "Reaumur", "C"), 1e-9)
        assertEquals(80.0, Units.convertTemp(100.0, "C", "Reaumur"), 1e-9)
    }

    @Test fun tempRomer() {
        assertEquals(0.0, Units.convertTemp(7.5, "Romer", "C"), 1e-9)
        assertEquals(100.0, Units.convertTemp(60.0, "Romer", "C"), 1e-9)
        assertEquals(7.5, Units.convertTemp(0.0, "C", "Romer"), 1e-9)
    }

    @Test fun tempDelisle() {
        assertEquals(100.0, Units.convertTemp(0.0, "Delisle", "C"), 1e-9)
        assertEquals(0.0, Units.convertTemp(150.0, "Delisle", "C"), 1e-9)
        assertEquals(150.0, Units.convertTemp(0.0, "C", "Delisle"), 1e-9)
    }

    @Test fun tempNewton() {
        assertEquals(100.0, Units.convertTemp(33.0, "Newton", "C"), 1e-9)
        assertEquals(33.0, Units.convertTemp(100.0, "C", "Newton"), 1e-9)
    }

    @Test fun gasMarkTable() {
        assertEquals(140.0, Units.gasMarkToC(1.0), 1e-9)
        assertEquals(180.0, Units.gasMarkToC(4.0), 1e-9)
        assertEquals(240.0, Units.gasMarkToC(9.0), 1e-9)
        assertEquals(4.0, Units.cToGasMark(180.0), 1e-9)
        assertEquals(6.0, Units.cToGasMark(200.0), 1e-9)
    }

    @Test fun paceExact() {
        assertEquals(0.06, Units.convert(1.0, Units.pace.getValue("min/km"), Units.pace.getValue("s/m")), 1e-12)
        assertEquals(60.0 / 1609.344, Units.convert(1.0, Units.pace.getValue("min/mi"), Units.pace.getValue("s/m")), 1e-12)
        assertEquals(3.6, Units.convert(1.0, Units.pace.getValue("h/km"), Units.pace.getValue("s/m")), 1e-12)
    }

    @Test fun diopterBase() {
        assertEquals(1.0, Units.convert(1.0, Units.opticalPower.getValue("diopter"), Units.opticalPower.getValue("dpt")), 1e-12)
    }

    @Test fun luminanceLadder() {
        assertEquals(10000.0, Units.convert(1.0, Units.luminance.getValue("stilb"), Units.luminance.getValue("nit")), 1e-6)
        assertEquals(10000.0 / Math.PI, Units.convert(1.0, Units.luminance.getValue("lambert"), Units.luminance.getValue("nit")), 1e-6)
        assertEquals(1.0 / (Math.PI * 0.09290304), Units.convert(1.0, Units.luminance.getValue("footlambert"), Units.luminance.getValue("nit")), 1e-9)
        assertEquals(1.0, Units.convert(1.0, Units.luminance.getValue("nit"), Units.luminance.getValue("cd/m2")), 1e-12)
    }

    @Test fun magFluxMaxwell() {
        assertEquals(1e-8, Units.convert(1.0, Units.magflux.getValue("Mx"), Units.magflux.getValue("Wb")), 1e-17)
    }

    @Test fun luminousUnits() {
        assertEquals(1.0, Units.convert(1.0, Units.luminous.getValue("lm"), Units.luminous.getValue("cd")), 1e-12)
    }

    @Test fun siPrefixLadder() {
        assertEquals(1e30, Units.prefixFactor("Q"), 1.0)
        assertEquals(1e-30, Units.prefixFactor("q"), 1e-42)
        assertEquals(1000.0, Units.prefixFactor("k"), 1e-12)
        assertEquals(0.01, Units.prefixFactor("c"), 1e-15)
    }

    @Test fun numberBaseTable() {
        assertEquals("1010", Units.convertNumberBase(10.0, "bin"))
        assertEquals("12", Units.convertNumberBase(10.0, "oct"))
        assertEquals("A", Units.convertNumberBase(10.0, "hex"))
        assertEquals("10", Units.convertNumberBase(10.0, "dec"))
        assertEquals("binary", Units.numberBaseLabels.getValue("bin"))
    }

    @Test fun quantityAdds() {
        assertEquals(13.0, Units.convert(1.0, Units.quantity.getValue("bakersDozen"), Units.quantity.getValue("count")), 1e-12)
        assertEquals(20.0, Units.convert(1.0, Units.quantity.getValue("score"), Units.quantity.getValue("count")), 1e-12)
        assertEquals(500.0, Units.convert(1.0, Units.quantity.getValue("ream"), Units.quantity.getValue("count")), 1e-12)
        assertEquals(1.0, Units.convert(1000.0, Units.quantity.getValue("permill"), Units.quantity.getValue("count")), 1e-12)
        assertEquals(1.0, Units.convert(24.0, Units.quantity.getValue("karat"), Units.concentration.getValue("fraction")), 1e-12)
    }

    @Test fun concentrationPpt() {
        assertEquals(1e12, Units.convert(1.0, Units.concentration.getValue("fraction"), Units.concentration.getValue("ppt")), 1.0)
    }

    @Test fun imperialVolumeExtras() {
        assertEquals(0.284130625, Units.convert(1.0, Units.volume.getValue("cupUK"), Units.volume.getValue("L")), 1e-12)
        assertEquals(0.25, Units.convert(1.0, Units.volume.getValue("cupMetric"), Units.volume.getValue("L")), 1e-12)
        assertEquals(0.02, Units.convert(1.0, Units.volume.getValue("tbspAU"), Units.volume.getValue("L")), 1e-12)
        assertEquals(0.005, Units.convert(1.0, Units.volume.getValue("tspMetric"), Units.volume.getValue("L")), 1e-12)
        assertEquals(0.11829411825, Units.convert(1.0, Units.volume.getValue("gillUS"), Units.volume.getValue("L")), 1e-12)
        assertEquals(0.1420653125, Units.convert(1.0, Units.volume.getValue("gillUK"), Units.volume.getValue("L")), 1e-12)
        assertEquals(163.65924, Units.convert(1.0, Units.volume.getValue("barrelUK"), Units.volume.getValue("L")), 1e-9)
    }

    @Test fun hundredweightBoth() {
        assertEquals(45.359237, Units.convert(1.0, Units.mass.getValue("cwtShort"), Units.mass.getValue("kg")), 1e-9)
        assertEquals(50.80234544, Units.convert(1.0, Units.mass.getValue("cwtLong"), Units.mass.getValue("kg")), 1e-9)
    }

    @Test fun torqueAdds() {
        assertEquals(0.0625, Units.convert(1.0, Units.torque.getValue("ozf·in"), Units.torque.getValue("lbf·in")), 1e-12)
        assertEquals(1e-5, Units.convert(1.0, Units.torque.getValue("dyne·m"), Units.torque.getValue("N·m")), 1e-14)
        assertEquals(0.01, Units.convert(1.0, Units.torque.getValue("N·cm"), Units.torque.getValue("N·m")), 1e-12)
        assertEquals(0.001, Units.convert(1.0, Units.torque.getValue("N·mm"), Units.torque.getValue("N·m")), 1e-12)
        assertEquals(0.0980665, Units.convert(1.0, Units.torque.getValue("kgf·cm"), Units.torque.getValue("N·m")), 1e-12)
        assertEquals(0.138254954376, Units.convert(1.0, Units.torque.getValue("poundal·m"), Units.torque.getValue("N·m")), 1e-12)
    }

    @Test fun densityLadder() {
        assertEquals(0.001, Units.convert(1.0, Units.density.getValue("mg/L"), Units.density.getValue("kg/m³")), 1e-12)
        assertEquals(0.01, Units.convert(1.0, Units.density.getValue("mg/dL"), Units.density.getValue("kg/m³")), 1e-12)
        assertEquals(10.0, Units.convert(1.0, Units.density.getValue("g/dL"), Units.density.getValue("kg/m³")), 1e-9)
    }

    @Test fun capacitanceLadder() {
        assertEquals(1e-18, Units.convert(1.0, Units.capacitance.getValue("aF"), Units.capacitance.getValue("F")), 1e-30)
        assertEquals(1e-15, Units.convert(1.0, Units.capacitance.getValue("fF"), Units.capacitance.getValue("F")), 1e-27)
        assertEquals(1000.0, Units.convert(1.0, Units.capacitance.getValue("kF"), Units.capacitance.getValue("F")), 1e-9)
        assertEquals(1e6, Units.convert(1.0, Units.capacitance.getValue("MF"), Units.capacitance.getValue("F")), 1e-3)
        assertEquals(1e9, Units.convert(1.0, Units.capacitance.getValue("GF"), Units.capacitance.getValue("F")), 1.0)
        assertEquals(1.112650056e-12, Units.convert(1.0, Units.capacitance.getValue("statF"), Units.capacitance.getValue("F")), 1e-21)
    }

    @Test fun frequencyMillihertz() {
        assertEquals(0.001, Units.convert(1.0, Units.frequency.getValue("mHz"), Units.frequency.getValue("Hz")), 1e-12)
    }

    @Test fun dataNibbleKibibit() {
        assertEquals(0.5, Units.convert(1.0, Units.data.getValue("nibble"), Units.data.getValue("B")), 1e-12)
        assertEquals(128.0, Units.convert(1.0, Units.data.getValue("Kibit"), Units.data.getValue("B")), 1e-9)
        assertEquals(131072.0, Units.convert(1.0, Units.data.getValue("Mibit"), Units.data.getValue("B")), 1e-6)
        assertEquals(134217728.0, Units.convert(1.0, Units.data.getValue("Gibit"), Units.data.getValue("B")), 1.0)
    }

    @Test fun lengthAdds() {
        assertEquals(1e-12, Units.convert(1.0, Units.length.getValue("pm"), Units.length.getValue("m")), 1e-24)
        assertEquals(1200.0 / 3937.0, Units.convert(1.0, Units.length.getValue("surveyFoot"), Units.length.getValue("m")), 1e-12)
        assertEquals(91.44, Units.convert(1.0, Units.length.getValue("footballField"), Units.length.getValue("m")), 1e-9)
        assertEquals(0.04445, Units.convert(1.0, Units.length.getValue("rackUnit"), Units.length.getValue("m")), 1e-12)
        assertEquals(10000.0, Units.convert(1.0, Units.length.getValue("swedishMil"), Units.length.getValue("m")), 1e-9)
    }

    @Test fun massAdds() {
        assertEquals(0.1, Units.convert(1.0, Units.mass.getValue("ettogram"), Units.mass.getValue("kg")), 1e-12)
        assertEquals(100.0, Units.convert(1.0, Units.mass.getValue("quintal"), Units.mass.getValue("kg")), 1e-9)
    }

    @Test fun timeAdds() {
        assertEquals(0.1, Units.convert(1.0, Units.time.getValue("ds"), Units.time.getValue("s")), 1e-12)
        assertEquals(0.01, Units.convert(1.0, Units.time.getValue("cs"), Units.time.getValue("s")), 1e-12)
    }

    @Test fun volumeCubic() {
        assertEquals(0.016387064, Units.convert(1.0, Units.volume.getValue("in3"), Units.volume.getValue("L")), 1e-12)
        assertEquals(28.316846592, Units.convert(1.0, Units.volume.getValue("ft3"), Units.volume.getValue("L")), 1e-9)
        assertEquals(764.554857984, Units.convert(1.0, Units.volume.getValue("yd3"), Units.volume.getValue("L")), 1e-6)
    }

    @Test fun areaAdds() {
        assertEquals(0.01, Units.convert(1.0, Units.area.getValue("dm2"), Units.area.getValue("m2")), 1e-12)
        assertEquals(1e-12, Units.convert(1.0, Units.area.getValue("um2"), Units.area.getValue("m2")), 1e-24)
    }

    @Test fun evEfficiency() {
        assertEquals(1.0 / 33.7, Units.convert(1.0, Units.ev.getValue("MPGe"), Units.ev.getValue("mi/kWh")), 1e-12)
        assertEquals(1000.0 / 1.609344 / 200.0, Units.convert(200.0, Units.ev.getValue("Wh/km"), Units.ev.getValue("mi/kWh")), 1e-9)
        assertEquals(200.0, Units.convert(1000.0 / 1.609344 / 200.0, Units.ev.getValue("mi/kWh"), Units.ev.getValue("Wh/km")), 1e-9)
    }

    @Test fun thermalRvalue() {
        assertEquals(1.0, Units.convert(1.0, Units.rvalue.getValue("RSI"), Units.rvalue.getValue("RSI")), 1e-12)
        assertEquals(0.1761101838, Units.convert(1.0, Units.rvalue.getValue("Rvalue"), Units.rvalue.getValue("RSI")), 1e-10)
    }

    @Test fun gasFlow() {
        assertEquals(1e-6 / 60.0, Units.convert(1.0, Units.gasflow.getValue("sccm"), Units.gasflow.getValue("m3/s")), 1e-17)
        assertEquals(1e-3 / 60.0, Units.convert(1.0, Units.gasflow.getValue("slpm"), Units.gasflow.getValue("m3/s")), 1e-14)
    }

    @Test fun conductanceSiemens() {
        assertEquals(1.0, Units.convert(1.0, Units.conductance.getValue("S"), Units.conductance.getValue("S")), 1e-12)
        assertEquals(1.0, Units.convert(1.0, Units.conductance.getValue("mho"), Units.conductance.getValue("S")), 1e-12)
        assertEquals(0.001, Units.convert(1.0, Units.conductance.getValue("mS"), Units.conductance.getValue("S")), 1e-12)
        assertEquals(1e-6, Units.convert(1.0, Units.conductance.getValue("uS"), Units.conductance.getValue("S")), 1e-15)
    }

    @Test fun solidHemisphere() {
        assertEquals(2 * Math.PI, Units.convert(1.0, Units.solidangle.getValue("hemisphere"), Units.solidangle.getValue("sr")), 1e-12)
    }

    @Test fun amountLbmol() {
        assertEquals(453.59237, Units.convert(1.0, Units.amount.getValue("lbmol"), Units.amount.getValue("mol")), 1e-9)
    }

    @Test fun schedulingManHour() {
        assertEquals(4.2, Units.scheduling.getValue("man-hour").toBase, 1e-12)
        assertEquals(40.0, Units.convert(1.0, Units.scheduling.getValue("man-week"), Units.scheduling.getValue("man-hour")), 1e-12)
    }

    @Test fun kmsAliasSingleFactor() {
        assertEquals(
            Units.convert(1.0, Units.speed.getValue("km/s"), Units.speed.getValue("m/s")),
            Units.convert(1.0, Units.speed.getValue("kms"), Units.speed.getValue("m/s")),
            0.0
        )
        assertEquals(1000.0, Units.convert(1.0, Units.speed.getValue("kms"), Units.speed.getValue("m/s")), 1e-9)
    }
}
