package calc.u.core

object TimeLab {
    data class Lap(val index: Int, val totalMs: Long, val splitMs: Long)

    data class PomoConfig(
        val focusMin: Int = 25,
        val shortMin: Int = 5,
        val longMin: Int = 15,
        val roundsUntilLong: Int = 4
    )

    fun formatHMS(ms: Long): String {
        val t = if (ms < 0) 0 else ms
        val h = t / 3600000
        val m = (t / 60000) % 60
        val s = (t / 1000) % 60
        val cs = (t % 1000) / 10
        return "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}.${cs.toString().padStart(2, '0')}"
    }

    fun addLap(laps: List<Long>, nowMs: Long): List<Lap> {
        val totals = laps + nowMs
        return totals.mapIndexed { i, total ->
            val prev = if (i == 0) 0L else totals[i - 1]
            Lap(index = i + 1, totalMs = total, splitMs = total - prev)
        }
    }

    fun pomoPhase(elapsedFocusSessions: Int, cfg: PomoConfig = PomoConfig()): String {
        if (elapsedFocusSessions <= 0) return "focus"
        val n = if (cfg.roundsUntilLong <= 0) 4 else cfg.roundsUntilLong
        return if (elapsedFocusSessions % n == 0) "long" else "short"
    }

    fun countdownParts(remainingMs: Long): Triple<Long, Long, Long> {
        val t = if (remainingMs < 0) 0 else remainingMs
        val min = t / 60000
        val sec = (t % 60000) / 1000
        val cs = (t % 1000) / 10
        return Triple(min, sec, cs)
    }

    data class AgeInfo(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val daysUntilBirthday: Long
    )

    fun safeWithYear(d: java.time.LocalDate, year: Int): java.time.LocalDate =
        runCatching { d.withYear(year) }.getOrDefault(d)

    fun ageOn(birth: java.time.LocalDate, today: java.time.LocalDate): AgeInfo {
        require(!birth.isAfter(today)) { "birth date is in the future" }
        val p = java.time.Period.between(birth, today)
        var next = safeWithYear(birth, today.year)
        while (!next.isAfter(today)) next = next.plusYears(1)
        return AgeInfo(
            p.years, p.months, p.days,
            java.time.temporal.ChronoUnit.DAYS.between(birth, today),
            java.time.temporal.ChronoUnit.DAYS.between(today, next)
        )
    }

    fun daysBetween(a: java.time.LocalDate, b: java.time.LocalDate): Long =
        java.time.temporal.ChronoUnit.DAYS.between(a, b)

    fun shiftDate(d: java.time.LocalDate, n: Long, unit: String, minus: Boolean): java.time.LocalDate {
        val moved = when (unit) {
            "Weeks" -> d.plusWeeks(n)
            "Months" -> d.plusMonths(n)
            "Years" -> d.plusYears(n)
            else -> d.plusDays(n)
        }
        return if (minus) d.plusDays(java.time.temporal.ChronoUnit.DAYS.between(moved, d)) else moved
    }
}

typealias Lap = TimeLab.Lap
typealias PomoConfig = TimeLab.PomoConfig

fun formatHMS(ms: Long): String = TimeLab.formatHMS(ms)
fun addLap(laps: List<Long>, nowMs: Long): List<TimeLab.Lap> = TimeLab.addLap(laps, nowMs)
fun pomoPhase(elapsedFocusSessions: Int, cfg: TimeLab.PomoConfig = TimeLab.PomoConfig()): String =
    TimeLab.pomoPhase(elapsedFocusSessions, cfg)

fun countdownParts(remainingMs: Long): Triple<Long, Long, Long> = TimeLab.countdownParts(remainingMs)
