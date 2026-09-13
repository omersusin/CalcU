package calc.u.core

// Text calculator (Soulver-style scratchpad).
// Interaction ideas after NerdCalci (GPL-3.0) — sequential line scope,
// `#` comments, syntax-token colors, copy/share-with-results. All code below
// is an original implementation written against CalcU's own Engine.eval;
// nothing is copied. Attribution: note in Settings attributions.

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

enum class TextTokenKind { Number, Variable, Function, Operator, Comment, Text }

data class TextToken(val start: Int, val end: Int, val kind: TextTokenKind)

data class TextLineResult(
    val index: Int,
    val input: String,
    val name: String?,
    val display: String,
    val error: String?
)

@Serializable
data class TextSession(val name: String, val body: String, val updatedAt: Long = 0L)

object TextCalc {
    const val MAX_LINES = 2000
    const val MAX_CHARS = 200_000
    const val MAX_SESSIONS = 100
    const val MAX_NAME_CHARS = 60

    private val NameRegex = Regex("[A-Za-z_][A-Za-z0-9_]*")
    private val AssignRegex = Regex("^([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+)$")
    private val EmptyAssignRegex = Regex("^([A-Za-z_][A-Za-z0-9_]*)\\s*=$")
    private val SciTailRegex = Regex("^[eE][0-9]+$")
    private val SessionJson = Json { ignoreUnknownKeys = true }

    private val Reserved = setOf(
        "PI", "E", "SIN", "COS", "TAN", "ASIN", "ACOS", "ATAN",
        "SINR", "COSR", "TANR", "SQRT", "CBRT", "LOG", "LOG10", "LN",
        "EXP", "ABS", "CEIL", "FLOOR", "ROUND", "TRUNC", "SIGNUM",
        "MIN", "MAX", "SUM", "AVG", "POW", "MOD", "FACT", "GCD", "LCM",
        "PREV", "TOTAL"
    )

    fun evaluateAll(text: String): List<TextLineResult> {
        val safe = text.take(MAX_CHARS)
        val lines = if (safe.isEmpty()) emptyList() else safe.split("\n").take(MAX_LINES)
        val vars = LinkedHashMap<String, BigDecimal>()
        var prev: BigDecimal? = null
        var total = BigDecimal.ZERO
        val out = ArrayList<TextLineResult>(lines.size)
        for ((i, raw) in lines.withIndex()) {
            if (Thread.currentThread().isInterrupted) {
                throw java.util.concurrent.CancellationException("evaluateAll cancelled")
            }
            val code = raw.substringBefore("#").trim()
            if (code.isEmpty()) {
                out.add(TextLineResult(i, raw, null, "", null))
                continue
            }
            val assign = AssignRegex.matchEntire(code)
            val name = assign?.groupValues?.getOrNull(1)
                ?: EmptyAssignRegex.matchEntire(code)?.groupValues?.getOrNull(1)
            val expr = (if (assign != null) assign.groupValues[2].trim() else code)
            if (name != null && name.uppercase() in Reserved) {
                out.add(TextLineResult(i, raw, name, "", "Name `$name` is reserved"))
                continue
            }
            if (name != null && (assign == null || expr.isEmpty())) {
                out.add(TextLineResult(i, raw, name, "", "Missing expression after `=`"))
                continue
            }
            val (row, value) = evalLine(i, raw, name, expr, vars, prev, total)
            out.add(row)
            if (value != null) {
                if (name != null) vars[name] = value
                prev = value
                total = total.add(value)
            }
        }
        return out
    }

    private fun evalLine(
        index: Int,
        raw: String,
        name: String?,
        expr: String,
        vars: Map<String, BigDecimal>,
        prev: BigDecimal?,
        total: BigDecimal
    ): Pair<TextLineResult, BigDecimal?> {
        val scope = LinkedHashMap<String, BigDecimal>()
        scope.putAll(vars)
        if (prev != null) scope["prev"] = prev
        scope["total"] = total
        val unknown = unknownVar(expr, scope.keys)
        if (unknown != null) {
            val msg = if (unknown == "prev") "`prev` has no value yet" else "Unknown variable `$unknown`"
            return Pair(TextLineResult(index, raw, name, "", msg), null)
        }
        val result = evalWithVars(expr, scope)
        val value = result.getOrNull()
        if (value == null) {
            return Pair(TextLineResult(index, raw, name, "", mapError(result.exceptionOrNull())), null)
        }
        if (!value.toDouble().isFinite()) {
            return Pair(TextLineResult(index, raw, name, "", "Result is not finite"), null)
        }
        return Pair(TextLineResult(index, raw, name, Engine.format(value), null), value)
    }

    private fun evalWithVars(expr: String, scope: Map<String, BigDecimal>): Result<BigDecimal> = runCatching {
        var e = expr
        for ((k, v) in scope.entries.sortedByDescending { it.key.length }) {
            val pat = Regex("(?<![A-Za-z0-9_])${Regex.escape(k)}(?![A-Za-z0-9_])")
            e = pat.replace(e, "(${v.toPlainString()})")
        }
        Engine.eval(e).getOrThrow()
    }

    private fun unknownVar(expr: String, known: Set<String>): String? {
        for (m in NameRegex.findAll(expr)) {
            val w = m.value
            val start = m.range.first
            if (start > 0 && (expr[start - 1].isDigit() || expr[start - 1] == '.') &&
                (w.equals("e", ignoreCase = true) || SciTailRegex.matches(w))
            ) {
                continue
            }
            var j = m.range.last + 1
            while (j < expr.length && expr[j].isWhitespace()) j++
            if (j < expr.length && expr[j] == '(') continue
            if (w.equals("PI", ignoreCase = true) || w.equals("E", ignoreCase = true)) continue
            if (w !in known) return w
        }
        return null
    }

    private fun mapError(t: Throwable?): String {
        val m = t?.message?.trim().orEmpty()
        if (m.contains("zero", ignoreCase = true)) return "Division by zero"
        if (m.isEmpty()) return "Cannot evaluate expression"
        return "Cannot evaluate: ${m.take(120)}"
    }

    fun tokenize(line: String): List<TextToken> {
        val out = ArrayList<TextToken>()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '#' -> {
                    out.add(TextToken(i, line.length, TextTokenKind.Comment))
                    break
                }
                c.isDigit() || (c == '.' && i + 1 < line.length && line[i + 1].isDigit()) -> {
                    val s = i
                    while (i < line.length && (line[i].isDigit() || line[i] == '.')) i++
                    out.add(TextToken(s, i, TextTokenKind.Number))
                }
                c.isLetter() || c == '_' -> {
                    val s = i
                    while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) i++
                    var j = i
                    while (j < line.length && line[j].isWhitespace()) j++
                    val kind = if (j < line.length && line[j] == '(') TextTokenKind.Function else TextTokenKind.Variable
                    out.add(TextToken(s, i, kind))
                }
                c in "+-*/^%=()×÷−" -> {
                    out.add(TextToken(i, i + 1, TextTokenKind.Operator))
                    i++
                }
                else -> {
                    out.add(TextToken(i, i + 1, TextTokenKind.Text))
                    i++
                }
            }
        }
        return out
    }

    fun renderWithResults(text: String): String {
        return evaluateAll(text).joinToString("\n") { r ->
            when {
                r.error != null -> "${r.input}  # ${r.error}"
                r.display.isEmpty() -> r.input
                else -> "${r.input} => ${r.display}"
            }
        }
    }

    fun encodeSessions(sessions: List<TextSession>): String =
        runCatching { SessionJson.encodeToString(sessions.take(MAX_SESSIONS)) }.getOrDefault("[]")

    fun decodeSessions(json: String): List<TextSession> =
        runCatching { SessionJson.decodeFromString<List<TextSession>>(json) }.getOrDefault(emptyList())

    fun cleanSessionName(name: String): String = name.trim().take(MAX_NAME_CHARS)
}
