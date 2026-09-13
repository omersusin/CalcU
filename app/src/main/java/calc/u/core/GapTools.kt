package calc.u.core

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Gap-fill pure-math tools derived as ideas-only from the CalcuLatto checklist.
 *
 * CalcuLatto is under Syncertica-SAL v1.0 (NOT open source): checklist titles
 * studied, no code copied. Every formula below is a standard published equation,
 * re-derived here with constants verified against the primary reference.
 *
 * Computation only: informational arithmetic plus category labels. Not medical
 * advice, no diagnosis, no treatment recommendation.
 */
object GapTools {

    /**
     * Rectangular aquarium/tank volume in liters.
     * Equation: V(L) = L(cm) * W(cm) * H(cm) / 1000 * fillFraction.
     */
    fun aquariumVolumeLiters(lengthCm: Double, widthCm: Double, heightCm: Double, fillFraction: Double = 1.0): Double {
        require(lengthCm.isFinite() && lengthCm > 0.0) { "lengthCm must be > 0" }
        require(widthCm.isFinite() && widthCm > 0.0) { "widthCm must be > 0" }
        require(heightCm.isFinite() && heightCm > 0.0) { "heightCm must be > 0" }
        require(fillFraction.isFinite() && fillFraction > 0.0 && fillFraction <= 1.0) { "fillFraction must be in (0, 1]" }
        return lengthCm * widthCm * heightCm / 1000.0 * fillFraction
    }

    /**
     * Body surface area (m^2), DuBois & DuBois 1916:
     * BSA = 0.007184 * W^0.425 * H^0.725 (W in kg, H in cm).
     */
    fun bsaDuBois(weightKg: Double, heightCm: Double): Double {
        require(weightKg.isFinite() && weightKg > 0.0) { "weightKg must be > 0" }
        require(heightCm.isFinite() && heightCm > 0.0) { "heightCm must be > 0" }
        return 0.007184 * weightKg.pow(0.425) * heightCm.pow(0.725)
    }

    /**
     * Waist-to-hip ratio: WHR = waistCm / hipCm. Pure ratio.
     */
    fun waistHipRatio(waistCm: Double, hipCm: Double): Double {
        require(waistCm.isFinite() && waistCm > 0.0) { "waistCm must be > 0" }
        require(hipCm.isFinite() && hipCm > 0.0) { "hipCm must be > 0" }
        return waistCm / hipCm
    }

    /**
     * WHR range label only (WHO cut-points: >0.90 male, >0.85 female elevated).
     * Returns "typical range" or "above typical cut-point". Not a diagnosis.
     */
    fun waistHipCategory(ratio: Double, male: Boolean): String {
        require(ratio.isFinite() && ratio > 0.0) { "ratio must be > 0" }
        val cut = if (male) 0.90 else 0.85
        return if (ratio <= cut) "typical range" else "above typical cut-point"
    }

    /**
     * Lean body mass (kg), Hume 1966:
     * male: 0.32810*W + 0.33929*H - 29.5336;
     * female: 0.29569*W + 0.41813*H - 43.2933 (W kg, H cm).
     */
    fun lbmHume(weightKg: Double, heightCm: Double, male: Boolean): Double {
        require(weightKg.isFinite() && weightKg > 0.0) { "weightKg must be > 0" }
        require(heightCm.isFinite() && heightCm > 0.0) { "heightCm must be > 0" }
        return if (male) 0.32810 * weightKg + 0.33929 * heightCm - 29.5336
        else 0.29569 * weightKg + 0.41813 * heightCm - 43.2933
    }

    /**
     * Relative fat mass (%), Woolcott & Bergman 2018:
     * male: 64 - 20*(H / waist); female: 76 - 20*(H / waist), same units (cm).
     */
    fun rfm(heightCm: Double, waistCm: Double, male: Boolean): Double {
        require(heightCm.isFinite() && heightCm > 0.0) { "heightCm must be > 0" }
        require(waistCm.isFinite() && waistCm > 0.0) { "waistCm must be > 0" }
        return (if (male) 64.0 else 76.0) - 20.0 * (heightCm / waistCm)
    }

    /**
     * Basal metabolic rate (kcal/day), Mifflin-St Jeor 1990:
     * male: 10*W + 6.25*H - 5*A + 5; female: 10*W + 6.25*H - 5*A - 161.
     */
    fun bmrMifflin(weightKg: Double, heightCm: Double, ageYears: Int, male: Boolean): Double {
        require(weightKg.isFinite() && weightKg > 0.0) { "weightKg must be > 0" }
        require(heightCm.isFinite() && heightCm > 0.0) { "heightCm must be > 0" }
        require(ageYears in 0..150) { "ageYears must be in 0..150" }
        return 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears + if (male) 5.0 else -161.0
    }

    /**
     * Basal metabolic rate (kcal/day), Harris-Benedict revised 1984:
     * male: 88.362 + 13.397*W + 4.799*H - 5.677*A;
     * female: 447.593 + 9.247*W + 3.098*H - 4.330*A.
     */
    fun bmrHarrisBenedict(weightKg: Double, heightCm: Double, ageYears: Int, male: Boolean): Double {
        require(weightKg.isFinite() && weightKg > 0.0) { "weightKg must be > 0" }
        require(heightCm.isFinite() && heightCm > 0.0) { "heightCm must be > 0" }
        require(ageYears in 0..150) { "ageYears must be in 0..150" }
        return if (male) 88.362 + 13.397 * weightKg + 4.799 * heightCm - 5.677 * ageYears
        else 447.593 + 9.247 * weightKg + 3.098 * heightCm - 4.330 * ageYears
    }

    /**
     * Office blood-pressure range label (AHA 2017 thresholds, mmHg):
     * Normal (<120 and <80), Elevated (120-129 and <80),
     * Stage 1 (130-139 or 80-89), Stage 2 (>=140 or >=90),
     * Crisis (>180 or >120). Label only, not a diagnosis.
     */
    fun bloodPressureCategory(systolic: Int, diastolic: Int): String {
        require(systolic in 40..320) { "systolic must be in 40..320" }
        require(diastolic in 20..220) { "diastolic must be in 20..220" }
        return when {
            systolic > 180 || diastolic > 120 -> "hypertensive crisis range"
            systolic >= 140 || diastolic >= 90 -> "stage 2 range"
            systolic >= 130 || diastolic >= 80 -> "stage 1 range"
            systolic >= 120 -> "elevated range"
            else -> "normal range"
        }
    }

    /**
     * Corrected QT (ms), Bazett 1920: QTc = QT(ms) / sqrt(RR(s)).
     */
    fun qtcBazett(qtMs: Double, rrSeconds: Double): Double {
        require(qtMs.isFinite() && qtMs > 0.0) { "qtMs must be > 0" }
        require(rrSeconds.isFinite() && rrSeconds > 0.0) { "rrSeconds must be > 0" }
        return qtMs / sqrt(rrSeconds)
    }

    /**
     * QTc from heart rate: RR(s) = 60 / hrBpm, then Bazett.
     */
    fun qtcFromHeartRate(qtMs: Double, heartRateBpm: Double): Double {
        require(qtMs.isFinite() && qtMs > 0.0) { "qtMs must be > 0" }
        require(heartRateBpm.isFinite() && heartRateBpm > 0.0) { "heartRateBpm must be > 0" }
        return qtcBazett(qtMs, 60.0 / heartRateBpm)
    }

    /**
     * HOMA-IR (Matthews et al. 1985): (insulin uU/mL * glucose mg/dL) / 405.
     * Pure index value; no threshold claim here.
     */
    fun homaIr(glucoseMgDl: Double, insulinMicroUml: Double): Double {
        require(glucoseMgDl.isFinite() && glucoseMgDl > 0.0) { "glucoseMgDl must be > 0" }
        require(insulinMicroUml.isFinite() && insulinMicroUml > 0.0) { "insulinMicroUml must be > 0" }
        return glucoseMgDl * insulinMicroUml / 405.0
    }

    /**
     * Serum anion gap (mEq/L): Na - (Cl + HCO3), or (Na + K) - (Cl + HCO3)
     * when potassium is supplied.
     */
    fun anionGap(sodium: Double, chloride: Double, bicarbonate: Double, potassium: Double? = null): Double {
        require(sodium.isFinite()) { "sodium must be finite" }
        require(chloride.isFinite()) { "chloride must be finite" }
        require(bicarbonate.isFinite()) { "bicarbonate must be finite" }
        if (potassium != null) require(potassium.isFinite() && potassium >= 0.0) { "potassium must be >= 0" }
        return (sodium + (potassium ?: 0.0)) - (chloride + bicarbonate)
    }

    /**
     * Corrected calcium (mg/dL), Payne 1973:
     * corrected = measured + 0.8 * (4.0 - albumin g/dL).
     */
    fun correctedCalcium(calciumMgDl: Double, albuminGDl: Double): Double {
        require(calciumMgDl.isFinite() && calciumMgDl > 0.0) { "calciumMgDl must be > 0" }
        require(albuminGDl.isFinite() && albuminGDl > 0.0) { "albuminGDl must be > 0" }
        return calciumMgDl + 0.8 * (4.0 - albuminGDl)
    }

    /**
     * LDL cholesterol (mg/dL), Friedewald 1972: LDL = TC - HDL - TG/5.
     * Valid only for fasting TG < 400 mg/dL.
     */
    fun ldlFriedewald(totalCholMgDl: Double, hdlMgDl: Double, triglyceridesMgDl: Double): Double {
        require(totalCholMgDl.isFinite() && totalCholMgDl > 0.0) { "totalCholMgDl must be > 0" }
        require(hdlMgDl.isFinite() && hdlMgDl > 0.0) { "hdlMgDl must be > 0" }
        require(triglyceridesMgDl.isFinite() && triglyceridesMgDl > 0.0) { "triglyceridesMgDl must be > 0" }
        require(triglyceridesMgDl < 400.0) { "triglyceridesMgDl must be < 400 for Friedewald" }
        return totalCholMgDl - hdlMgDl - triglyceridesMgDl / 5.0
    }
}
