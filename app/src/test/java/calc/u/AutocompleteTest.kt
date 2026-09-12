package calc.u

import calc.u.core.AutocompleteIndex
import calc.u.core.Suggestion
import calc.u.core.Trie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AutocompleteTest {
    private fun sampleFunctions(): List<Suggestion> = listOf(
        Suggestion.function("sin", "Sine", "Trigonometric sine"),
        Suggestion.function("sinh", "Hyperbolic sine", "Hyperbolic sine"),
        Suggestion.function("sqrt", "Square root", "Square root"),
        Suggestion.function("cos", "Cosine", "Trigonometric cosine")
    )

    private fun sampleUnits(): List<Suggestion> = listOf(
        Suggestion.unit("m", "Meter", "Length"),
        Suggestion.unit("km", "Kilometer", "Length"),
        Suggestion.unit("tbsp_metric", "Metric tablespoon", "Volume"),
        Suggestion.currency("USD", "US dollar", "Currency")
    )

    private fun buildSample(): Trie =
        AutocompleteIndex.build(sampleFunctions(), sampleUnits())

    @Test fun trieInsertAndSearchBasics() {
        val trie = Trie()
        trie.insert("sin", "sin")
        trie.insert("sinh", "sinh")
        trie.insert("sqrt", "sqrt")
        trie.insert("cos", "cos")
        val si = trie.search("si").toMap()
        assertEquals(setOf("sin", "sinh"), si.keys)
        assertEquals(3, si["sin"])
        assertEquals(4, si["sinh"])
        val s = trie.search("s").map { it.first }.toSet()
        assertEquals(setOf("sin", "sinh", "sqrt"), s)
        assertTrue(trie.search("zzz").isEmpty())
    }

    @Test fun trieSearchIsCaseInsensitive() {
        val trie = Trie()
        trie.insert("Sin", "Sin")
        assertEquals(listOf(Pair("Sin", 3)), trie.search("si"))
        assertEquals(listOf(Pair("Sin", 3)), trie.search("SI"))
    }

    @Test fun trieUnderscorePrefixSearch() {
        val trie = Trie()
        trie.insert("tbsp_metric", "tbsp_metric")
        assertEquals(listOf(Pair("tbsp_metric", 11)), trie.search("tbsp_"))
        assertTrue(trie.search("tbsp__").isEmpty())
    }

    @Test fun queryFindsSinFunction() {
        buildSample()
        val result = AutocompleteIndex.query("si", 2)
        assertEquals("si", result.relevantText)
        assertEquals(0, result.start)
        assertEquals(2, result.end)
        val sin = result.items.firstOrNull { it.name == "sin" } ?: error("expected sin suggestion")
        assertEquals("sin(", sin.insertBefore)
    }

    @Test fun functionInsertionWrapsParens() {
        buildSample()
        val result = AutocompleteIndex.query("si", 2)
        val sin = result.items.first { it.name == "sin" }
        assertEquals("sin(", sin.insertBefore)
        assertEquals(")", sin.insertAfter)
        val applied = "si".substring(0, result.start) + sin.insertBefore + sin.insertAfter + "si".substring(result.end)
        assertEquals("sin()", applied)
    }

    @Test fun unitInsertionIsVerbatim() {
        buildSample()
        val result = AutocompleteIndex.query("2 k", 3)
        assertEquals("k", result.relevantText)
        assertEquals(2, result.start)
        assertEquals(3, result.end)
        val km = result.items.first { it.name == "km" }
        assertEquals("km", km.insertBefore)
        assertEquals("", km.insertAfter)
        val applied = "2 k".substring(0, result.start) + km.insertBefore + km.insertAfter + "2 k".substring(result.end)
        assertEquals("2 km", applied)
    }

    @Test fun queryNoMatchIsEmpty() {
        buildSample()
        val result = AutocompleteIndex.query("zzz", 3)
        assertEquals("zzz", result.relevantText)
        assertEquals(0, result.start)
        assertEquals(3, result.end)
        assertTrue(result.items.isEmpty())
    }

    @Test fun queryUsesTrailingLetterRunBeforeCursor() {
        buildSample()
        val tail = AutocompleteIndex.query("sin+co", 6)
        assertEquals("co", tail.relevantText)
        assertEquals(4, tail.start)
        assertEquals(6, tail.end)
        assertTrue(tail.items.any { it.name == "cos" })
        val mid = AutocompleteIndex.query("sincos", 3)
        assertEquals("sin", mid.relevantText)
        assertEquals(0, mid.start)
        assertEquals(3, mid.end)
        val digits = AutocompleteIndex.query("2+3", 3)
        assertEquals("", digits.relevantText)
        assertEquals(3, digits.start)
        assertEquals(3, digits.end)
        assertTrue(digits.items.isEmpty())
    }

    @Test fun queryFindsUnderscoreNames() {
        buildSample()
        val result = AutocompleteIndex.query("tbsp_m", 6)
        assertEquals("tbsp_m", result.relevantText)
        assertEquals(0, result.start)
        assertEquals(6, result.end)
        val hit = result.items.firstOrNull { it.name == "tbsp_metric" } ?: error("expected tbsp_metric suggestion")
        assertEquals("tbsp_metric", hit.insertBefore)
        assertEquals("", hit.insertAfter)
        val long = AutocompleteIndex.query("2*tbsp_metric", 13)
        assertEquals("tbsp_metric", long.relevantText)
        assertEquals(2, long.start)
        assertEquals(13, long.end)
        assertTrue(long.items.any { it.name == "tbsp_metric" })
    }
}
