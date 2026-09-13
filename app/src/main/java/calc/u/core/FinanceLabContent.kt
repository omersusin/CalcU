package calc.u.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard

@Composable
fun FinanceLabContent(onCopy: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CompoundGrowthCard(onCopy)
        SavingsGoalCard(onCopy)
        LoanAmortizationCard(onCopy)
    }
}

@Composable
private fun CompoundGrowthCard(onCopy: (String) -> Unit) {
    var principal by remember { mutableStateOf("1000") }
    var ratePct by remember { mutableStateOf("5") }
    var years by remember { mutableStateOf("10") }
    var monthlyContribution by remember { mutableStateOf("0") }

    val p = (principal.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val r = (ratePct.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val y = (years.toIntOrNull() ?: 0).coerceIn(0, 120)
    val c = (monthlyContribution.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)

    val schedule = remember(p, r, y, c) {
        runCatching { Finance.compoundSchedule(p, r, y, c) }.getOrElse { emptyList() }
    }
    val finalBalance = schedule.lastOrNull()?.second ?: Double.NaN

    SectionCard(title = "Compound growth") {
        CalcUNumberBox(principal, { principal = it }, "Principal")
        CalcUNumberBox(ratePct, { ratePct = it }, "Rate (% per year)")
        CalcUNumberBox(years, { years = it }, "Years", integer = true)
        CalcUNumberBox(monthlyContribution, { monthlyContribution = it }, "Monthly contribution")
        ResultLine("Final balance", fmtMoney(finalBalance))
        BalanceBars(schedule)
        CopyButton(onCopy, "Final balance at compound growth: ${fmtMoney(finalBalance)}")
    }
}

@Composable
private fun SavingsGoalCard(onCopy: (String) -> Unit) {
    var goal by remember { mutableStateOf("50000") }
    var ratePct by remember { mutableStateOf("6") }
    var years by remember { mutableStateOf("10") }

    val goalV = (goal.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val r = (ratePct.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val y = (years.toIntOrNull() ?: 0).coerceIn(0, 120)

    val deposit = remember(goalV, r, y) {
        runCatching { Finance.savingsGoalMonthly(goalV, r, y) }.getOrElse { Double.NaN }
    }

    SectionCard(title = "Savings goal") {
        CalcUNumberBox(goal, { goal = it }, "Goal amount")
        CalcUNumberBox(ratePct, { ratePct = it }, "Rate (% per year)")
        CalcUNumberBox(years, { years = it }, "Years", integer = true)
        ResultLine("Monthly deposit needed", fmtMoney(deposit))
        CopyButton(onCopy, "Monthly deposit for goal ${fmtMoney(goalV)}: ${fmtMoney(deposit)}")
    }
}

@Composable
private fun LoanAmortizationCard(onCopy: (String) -> Unit) {
    var principal by remember { mutableStateOf("200000") }
    var ratePct by remember { mutableStateOf("6") }
    var years by remember { mutableStateOf("30") }

    val p = (principal.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val r = (ratePct.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val y = (years.toIntOrNull() ?: 1).coerceIn(1, 100)

    val payment = remember(p, r, y) {
        runCatching { Finance.monthlyPayment(p, r, y) }.getOrElse { Double.NaN }
    }
    val schedule = remember(p, r, y) {
        runCatching { Finance.loanAmortization(p, r, y) }.getOrElse { emptyList() }
    }

    SectionCard(title = "Loan amortization") {
        CalcUNumberBox(principal, { principal = it }, "Loan principal")
        CalcUNumberBox(ratePct, { ratePct = it }, "Rate (% per year)")
        CalcUNumberBox(years, { years = it }, "Years", integer = true)
        ResultLine("Monthly payment", fmtMoney(payment))
        BalanceBars(schedule)
        for ((year, remaining) in schedule) {
            Text(
                "Year $year - ${fmtMoney(remaining)} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CopyButton(
            onCopy,
            "Monthly payment ${fmtMoney(payment)} for ${fmtMoney(p)} over $y years"
        )
    }
}

@Composable
private fun BalanceBars(data: List<Pair<Int, Double>>) {
    val barColor = MaterialTheme.colorScheme.primary
    val maxVal = data.maxOfOrNull { it.second } ?: 0.0
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        if (data.isEmpty() || maxVal <= 0.0) return@Canvas
        val slot = size.width / data.size
        var i = 0
        for ((_, v) in data) {
            val frac = (v / maxVal).coerceIn(0.0, 1.0).toFloat()
            val barHeight = size.height * frac
            val left = slot * i + slot * 0.2f
            drawRect(
                color = barColor,
                alpha = 0.35f + 0.65f * frac,
                topLeft = Offset(left, size.height - barHeight),
                size = Size(slot * 0.6f, barHeight)
            )
            i++
        }
    }
}

@Composable
private fun CopyButton(onCopy: (String) -> Unit, text: String) {
    TextButton(onClick = { onCopy(text) }) {
        Text("Copy")
    }
}

private fun fmt(v: Double): String =
    if (v.isFinite()) String.format(java.util.Locale.US, "%.2f", v) else "-"

private fun fmtMoney(v: Double): String = "$${fmt(v)}"