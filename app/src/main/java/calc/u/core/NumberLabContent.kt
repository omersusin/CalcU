package calc.u.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard

@Composable
fun NumberLabContent(onCopy: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimeFactorsCard(onCopy)
        ModularSequenceCard(onCopy)
        CombinatoricsCard(onCopy)
    }
}

@Composable
private fun PrimeFactorsCard(onCopy: (String) -> Unit) {
    var nIn by remember { mutableStateOf("84") }
    var mIn by remember { mutableStateOf("36") }

    val n = nIn.toLongOrNull()?.coerceIn(0, 1_000_000) ?: 0
    val m = mIn.toLongOrNull()?.coerceIn(0, 1_000_000) ?: 0

    val prime = remember(n) { runCatching { Engine.isPrime(n) }.getOrNull() }
    val factorization = remember(n) {
        if (n >= 2) runCatching { NumberTheory.factorString(n) }.getOrNull() else null
    }
    val distinct = remember(n) {
        if (n >= 2) runCatching { NumberTheory.primeFactors(n).distinct().size }.getOrNull() else null
    }
    val phi = remember(n) {
        if (n >= 1) runCatching { NumberTheory.totient(n) }.getOrNull() else null
    }
    val gcdV = remember(n, m) { runCatching { Engine.gcd(n, m) }.getOrNull() }
    val lcmV = remember(n, m) { runCatching { Engine.lcm(n, m) }.getOrNull() }

    SectionCard(title = "Prime & factors") {
        CalcUNumberBox(nIn, { nIn = it }, "n", integer = true)
        CalcUNumberBox(mIn, { mIn = it }, "m", integer = true)
        ResultLine("Is prime", when (prime) {
            true -> "yes"
            false -> "no"
            null -> "—"
        })
        ResultLine("Prime factorization", factorization?.let { "n = $it" } ?: "—")
        ResultLine("φ(n) totient", phi?.toString() ?: "—")
        ResultLine("Distinct prime factors", distinct?.toString() ?: "—")
        ResultLine("GCD(n, m)", gcdV?.toString() ?: "—")
        ResultLine("LCM(n, m)", lcmV?.toString() ?: "—")
        CopyButton(
            onCopy,
            factorization?.let { "n = $n = $it; φ(n) = $phi; gcd = $gcdV; lcm = $lcmV" } ?: "n = $n"
        )
    }
}

@Composable
private fun ModularSequenceCard(onCopy: (String) -> Unit) {
    var nIn by remember { mutableStateOf("21") }
    var modIn by remember { mutableStateOf("26") }

    val n = nIn.toLongOrNull()?.coerceIn(0, 1_000_000) ?: 0
    val mod = modIn.toLongOrNull()?.coerceIn(0, 1_000_000) ?: 0

    val coprime = remember(n, mod) {
        mod >= 2 && (runCatching { Engine.gcd(n, mod) }.getOrNull() ?: 0L) == 1L
    }
    val inverse = remember(n, mod) {
        when {
            mod < 2 -> "mod must be > 1"
            !coprime -> "not coprime"
            else -> runCatching { NumberTheory.modInverse(n, mod).toString() }.getOrElse { "too large" }
        }
    }
    val fib = remember(n) {
        when {
            n > 92 -> "too large"
            else -> runCatching { NumberTheory.fibonacci(n.toInt()).toString() }.getOrElse { "—" }
        }
    }
    val collatz = remember(n) {
        if (n >= 1) {
            runCatching { NumberTheory.collatzSteps(n).toString() }.getOrElse { "—" }
        } else {
            "n must be > 0"
        }
    }

    SectionCard(title = "Modular & sequence") {
        CalcUNumberBox(nIn, { nIn = it }, "n", integer = true)
        CalcUNumberBox(modIn, { modIn = it }, "modulus", integer = true)
        ResultLine("Inverse of n mod m", inverse)
        ResultLine("Fibonacci F(n)", fib)
        ResultLine("Collatz steps to 1", collatz)
        CopyButton(onCopy, "n = $n, mod $mod: inverse $inverse, F(n) = $fib, collatz steps $collatz")
    }
}

@Composable
private fun CombinatoricsCard(onCopy: (String) -> Unit) {
    var nIn by remember { mutableStateOf("10") }
    var rIn by remember { mutableStateOf("3") }

    val n = nIn.toLongOrNull()?.coerceIn(0, 60) ?: 0
    val r = (rIn.toLongOrNull() ?: 0).coerceIn(0L, n)

    val combos = remember(n, r) { runCatching { Engine.nCr(n, r).toString() }.getOrElse { "too large" } }
    val perms = remember(n, r) { runCatching { Engine.nPr(n, r).toString() }.getOrElse { "too large" } }
    val fact = remember(n) {
        if (n <= 20) runCatching { Engine.factorial(n).toString() }.getOrElse { "too large" } else "too large"
    }

    SectionCard(title = "Combinatorics") {
        CalcUNumberBox(nIn, { nIn = it }, "n", integer = true)
        CalcUNumberBox(rIn, { rIn = it }, "r", integer = true)
        ResultLine("C(n, r)", combos)
        ResultLine("P(n, r)", perms)
        ResultLine("n!", fact)
        CopyButton(onCopy, "n = $n, r = $r: C(n, r) = $combos, P(n, r) = $perms, n! = $fact")
    }
}

@Composable
private fun CopyButton(onCopy: (String) -> Unit, text: String) {
    TextButton(onClick = { onCopy(text) }) {
        Text("Copy")
    }
}