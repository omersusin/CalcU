package calc.u

import calc.u.core.Programmer
import calc.u.core.Programmer.BitWidth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ProgrammerP3Test {
    private fun expectIAE(block: () -> Unit): String {
        try {
            block()
        } catch (e: IllegalArgumentException) {
            return e.message ?: ""
        }
        fail("expected IllegalArgumentException")
        return ""
    }

    @Test fun nandTruthTable() {
        // Single-bit truth (low bit): 0,0->1; 0,1->1; 1,0->1; 1,1->0.
        // On full Long, (a & b).inv() sets all high bits, so compare low bit + identity.
        assertEquals(1L, Programmer.nand(0, 0) and 1L)
        assertEquals(1L, Programmer.nand(0, 1) and 1L)
        assertEquals(1L, Programmer.nand(1, 0) and 1L)
        assertEquals(0L, Programmer.nand(1, 1) and 1L)
        assertEquals(-1L, Programmer.nand(0, 0))
        assertEquals(-2L, Programmer.nand(1, 1))
        assertEquals((12L and 5L).inv(), Programmer.nand(12, 5))
    }

    @Test fun norTruthTable() {
        assertEquals(1L, Programmer.nor(0, 0) and 1L)
        assertEquals(0L, Programmer.nor(0, 1) and 1L)
        assertEquals(0L, Programmer.nor(1, 0) and 1L)
        assertEquals(0L, Programmer.nor(1, 1) and 1L)
        assertEquals(-1L, Programmer.nor(0, 0))
        assertEquals(-2L, Programmer.nor(0, 1))
        assertEquals((12L or 5L).inv(), Programmer.nor(12, 5))
    }

    @Test fun xnorTruthTable() {
        assertEquals(1L, Programmer.xnor(0, 0) and 1L)
        assertEquals(0L, Programmer.xnor(0, 1) and 1L)
        assertEquals(0L, Programmer.xnor(1, 0) and 1L)
        assertEquals(1L, Programmer.xnor(1, 1) and 1L)
        assertEquals(-1L, Programmer.xnor(0, 0))
        assertEquals(-1L, Programmer.xnor(7, 7))
        assertEquals(-2L, Programmer.xnor(7, 6))
        assertEquals((12L xor 5L).inv(), Programmer.xnor(12, 5))
    }

    @Test fun andOrXorNotIdentities() {
        assertEquals(12L and 5L, Programmer.and(12, 5))
        assertEquals(12L or 5L, Programmer.or(12, 5))
        assertEquals(12L xor 5L, Programmer.xor(12, 5))
        assertEquals(12L.inv(), Programmer.not(12))
        assertEquals(-1L, Programmer.or(-1, 0))
        assertEquals(0L, Programmer.and(-1, 0))
    }

    @Test fun unsignedShiftIsLogical() {
        assertEquals(-1L ushr 1, Programmer.ushr(-1, 1))
        assertEquals(1L, Programmer.ushr(-1, 63))
        assertEquals(0L, Programmer.ushr(-1, 64))
        assertEquals(0x7FFFFFFFFFFFFFFFL, Programmer.ushr(-1, 1))
        assertTrue(Programmer.ushr(-8, 2) >= 0)
        assertEquals((-8L) ushr 2, Programmer.ushr(-8, 2))
    }

    @Test fun shiftsAtAndBeyondWidthAreZero() {
        for (w in BitWidth.entries) {
            assertEquals(0L, Programmer.shl(1, w.bits, w))
            assertEquals(0L, Programmer.shl(-1, w.bits + 1, w))
            assertEquals(0L, Programmer.ushr(1, w.bits, w))
            if (w == BitWidth.W64) {
                assertEquals(-1L, Programmer.shr(-1, 64, w))
                assertEquals(0L, Programmer.shr(1, 64, w))
            } else {
                assertEquals(Programmer.maskOf(w), Programmer.shr(-1, w.bits, w))
                assertEquals(0L, Programmer.shr(1, w.bits, w))
            }
        }
        assertTrue(expectIAE { Programmer.shl(1, -1) }.isNotBlank())
        assertTrue(expectIAE { Programmer.shr(1, -2) }.isNotBlank())
        assertTrue(expectIAE { Programmer.ushr(1, -3) }.isNotBlank())
        assertTrue(expectIAE { Programmer.rol(1, -1) }.isNotBlank())
        assertTrue(expectIAE { Programmer.ror(1, -1) }.isNotBlank())
    }

    @Test fun negativeShiftsAndOverflow() {
        assertEquals(0L, Programmer.shl(Long.MAX_VALUE, 64))
        assertEquals(0L, Programmer.shl(1, 63, BitWidth.W8))
        assertEquals(0x80L, Programmer.shl(1, 7, BitWidth.W8))
        assertEquals(0L, Programmer.shl(1, 8, BitWidth.W8))
        assertEquals(Programmer.maskOf(BitWidth.W8), Programmer.shr(-1, 0, BitWidth.W8))
        assertEquals(0xFFL, Programmer.ushr(-1, 0, BitWidth.W8))
    }

    @Test fun rotateRoundTripAndWidth() {
        assertEquals(1L, Programmer.rol(1, 64))
        assertEquals(1L, Programmer.ror(1, 64))
        assertEquals(-1L, Programmer.rol(-1, 17))
        assertEquals(0x81L, Programmer.rol(0x81L, 8, BitWidth.W8))
        assertEquals(0x81L, Programmer.ror(0x81L, 8, BitWidth.W8))
        assertEquals(0x02L, Programmer.rol(0x01L, 1, BitWidth.W8))
        assertEquals(0x80L, Programmer.ror(0x01L, 1, BitWidth.W8))
        val v = 0x12345678L
        assertEquals(v, Programmer.ror(Programmer.rol(v, 13), 13))
        assertEquals(v, Programmer.rol(Programmer.ror(v, 13), 13))
        assertEquals(0xABCDL, Programmer.rol(0xABCDL, 16, BitWidth.W16))
        assertEquals(0x80000000L, Programmer.rol(0x00000001L, 31, BitWidth.W32))
        assertEquals(Long.MIN_VALUE, Programmer.rol(1L, 63))
        assertEquals(1L, Programmer.ror(Long.MIN_VALUE, 63))
    }

    @Test fun maskingAndTwosComplement() {
        assertEquals(0xFFL, Programmer.mask(0x1FFL, BitWidth.W8))
        assertEquals(0xFFFFL, Programmer.mask(-1L, BitWidth.W16))
        assertEquals(-1L, Programmer.signed(0xFFL, BitWidth.W8))
        assertEquals(-128L, Programmer.signed(0x80L, BitWidth.W8))
        assertEquals(127L, Programmer.signed(0x7FL, BitWidth.W8))
        assertEquals(-1L, Programmer.signed(-1L, BitWidth.W64))
        assertEquals(0L, Programmer.signed(0L, BitWidth.W32))
        assertEquals(-32768L, Programmer.signed(0x8000L, BitWidth.W16))
        assertEquals(255L, Programmer.unsigned(-1L, BitWidth.W8))
        assertEquals(4294967295L, Programmer.unsigned(-1L, BitWidth.W32))
        assertEquals("11111111", Programmer.toBinaryString(-1, BitWidth.W8))
        assertEquals("10000000", Programmer.toBinaryString(-128, BitWidth.W8))
        assertEquals(64, Programmer.toBinaryString(-1, BitWidth.W64).length)
        assertTrue(expectIAE { BitWidth.of(7) }.isNotBlank())
        assertEquals(BitWidth.W32, BitWidth.of(32))
    }

    @Test fun baseConversionRoundTrip() {
        assertEquals("FF", Programmer.toBase(255, 16))
        assertEquals("377", Programmer.toBase(255, 8))
        assertEquals("11111111", Programmer.toBase(255, 2))
        assertEquals("255", Programmer.toBase(255, 10))
        assertEquals("Z", Programmer.toBase(35, 36))
        assertEquals("10", Programmer.toBase(36, 36))
        assertEquals(255L, Programmer.parse("FF", 16))
        assertEquals(255L, Programmer.parse("0xFF", 16))
        assertEquals(5L, Programmer.parse("0b101", 2))
        assertEquals(8L, Programmer.parse("0o10", 8))
        assertEquals(-42L, Programmer.parse("-42", 10))
        assertEquals(42L, Programmer.parse("+42", 10))
        assertEquals(35L, Programmer.parse("z", 36))
        assertEquals("FF", Programmer.toBase(Programmer.parse("ff", 16), 16))
        assertTrue(expectIAE { Programmer.toBase(1, 1) }.isNotBlank())
        assertTrue(expectIAE { Programmer.toBase(1, 37) }.isNotBlank())
        assertTrue(expectIAE { Programmer.parse("", 10) }.isNotBlank())
        assertTrue(expectIAE { Programmer.parse("ZZ", 10) }.isNotBlank())
    }

    @Test fun fractionalRejectedDocumented() {
        val msg = expectIAE { Programmer.parse("3.5", 10) }
        assertTrue(msg.contains("fractional"))
        assertTrue(expectIAE { Programmer.parse("1e3", 10) }.isNotBlank())
        assertTrue(expectIAE { Programmer.parse("0x1.8", 16) }.isNotBlank())
    }

    @Test fun asciiAndUnicodeViews() {
        assertEquals(8, Programmer.asciiView(0x4142434445464748L).length)
        assertEquals("ABCDEFGH", Programmer.asciiView(0x4142434445464748L))
        assertEquals("........", Programmer.asciiView(0L))
        assertEquals("A", Programmer.unicodeChar(0x41L))
        assertEquals("A", Programmer.views(0x41L, BitWidth.W8).ascii.trim().takeLast(1))
        assertNull(Programmer.unicodeChar(0xD800L))
        assertNull(Programmer.unicodeChar(0xDFFFL))
        assertNull(Programmer.unicodeChar(-1L))
        assertNull(Programmer.unicodeChar(0x110000L))
        assertEquals("41", Programmer.views(0x41L, BitWidth.W8).hex)
        assertEquals("65", Programmer.views(0x41L, BitWidth.W8).signedDec)
        assertEquals("01000001", Programmer.views(0x41L, BitWidth.W8).bin)
    }

    @Test fun maskedOpsNarrowWidths() {
        assertEquals(0x00L, Programmer.nand(0xFFL, 0xFFL, BitWidth.W8))
        assertEquals(0xFFL, Programmer.nand(0xF0L, 0x0FL, BitWidth.W8))
        assertEquals(0x00L, Programmer.nor(0xFFL, 0x00L, BitWidth.W8))
        assertEquals(0xFFL, Programmer.xnor(0xA5L, 0xA5L, BitWidth.W8))
        assertEquals(0x00L, Programmer.not(0xFFL, BitWidth.W8))
        assertEquals(0xFFFFL, Programmer.not(0L, BitWidth.W16))
        assertEquals(0xFFFFFFFFL, Programmer.not(0L, BitWidth.W32))
    }
}
