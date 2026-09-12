package calc.u

import calc.u.core.FunTools
import org.junit.Assert.*
import org.junit.Test

class FunToolsTest {
    @Test fun colorBoundsAndHexFormat() {
        val hexRegex = Regex("#[0-9A-F]{6}")
        repeat(50) {
            val (r, g, b) = FunTools.randomColor()
            assertTrue(r in 0..255)
            assertTrue(g in 0..255)
            assertTrue(b in 0..255)
            assertTrue(hexRegex.matches(FunTools.toHex(Triple(r, g, b))))
        }
        assertEquals("#FF0010", FunTools.toHex(Triple(255, 0, 16)))
        assertEquals("#000000", FunTools.toHex(Triple(0, 0, 0)))
    }

    @Test fun symbolsNonEmptyUniqueNames() {
        val table = FunTools.symbolsTable()
        assertTrue(table.size >= 55)
        val names = table.map { it.first }
        assertEquals(names.size, names.toSet().size)
        for ((name, glyph) in table) {
            assertTrue(name.isNotBlank())
            assertTrue(glyph.isNotBlank())
        }
        val glyphs = table.map { it.second }.toSet()
        for (g in listOf("€", "£", "¥", "₹", "₩", "₺", "±", "×", "÷", "√", "∞", "∑", "∫", "π", "→", "←", "↑", "↓", "↔", "✓", "✕", "©", "®", "♂", "♀")) {
            assertTrue("missing glyph $g", g in glyphs)
        }
    }

    @Test fun rouletteReturnsMember() {
        val items = listOf("a", "b", "c")
        repeat(20) {
            assertTrue(FunTools.roulettePick(items) in items)
        }
        assertTrue(FunTools.roulettePick(items, seed = 42L) in items)
        try {
            FunTools.roulettePick(listOf("only"))
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
        }
        try {
            FunTools.roulettePick(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9"))
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test fun rangeBoundsRespected() {
        repeat(50) {
            val v = FunTools.rangeRandom(1, 6)
            assertTrue(v in 1..6)
        }
        assertEquals(5, FunTools.rangeRandom(5, 5))
        assertEquals(-999, FunTools.rangeRandom(-999, -999))
        try {
            FunTools.rangeRandom(6, 1)
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
        }
        try {
            FunTools.rangeRandom(-1000, 0)
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
        }
        try {
            FunTools.rangeRandom(0, 10000)
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test fun passwordLengthAndPoolGuarantees() {
        val specialsPool = "!@#\$%^&*()-_=+[]{};:,.<>?/~".toSet()
        repeat(20) {
            val pw = FunTools.passwordPlus(12, 3, 2, false)
            assertEquals(12, pw.length)
            assertEquals(3, pw.count { it.isDigit() })
            assertEquals(2, pw.count { it in specialsPool })
            assertTrue(pw.all { it.isLetterOrDigit() || it in specialsPool })
            assertTrue(pw.none { it in "Il1O0" })
        }
        val full = FunTools.passwordPlus(16, 4, 4, true)
        assertEquals(16, full.length)
        assertEquals(4, full.count { it.isDigit() })
        assertEquals(4, full.count { it in specialsPool })
        try {
            FunTools.passwordPlus(3, 2, 2, false)
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
        }
        try {
            FunTools.passwordPlus(65, 0, 0, false)
            fail("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
        }
    }
}
