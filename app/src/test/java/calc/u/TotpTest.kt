package calc.u

import calc.u.core.Totp
import org.junit.Assert.*
import org.junit.Test

class TotpTest {
    private val sha1Secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"

    @Test fun base32DecodesToAscii() {
        assertArrayEquals(
            "12345678901234567890".toByteArray(Charsets.UTF_8),
            Totp.base32Decode(sha1Secret)
        )
    }

    @Test fun rfc6238Sha1Vectors() {
        assertEquals("287082", Totp.totp(sha1Secret, 59))
        assertEquals("081804", Totp.totp(sha1Secret, 1111111109))
    }

    @Test fun secondsRemainingVector() {
        assertEquals(1L, Totp.secondsRemaining(59, 30))
    }

    @Test fun badBase32Throws() {
        try {
            Totp.base32Decode("")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Totp.base32Decode("!!!!")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Totp.totp("!!!!", 59)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun badPeriodSafe() {
        try {
            Totp.totp(sha1Secret, 59, 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals(0L, Totp.secondsRemaining(59, 0))
        assertEquals(0L, Totp.secondsRemaining(59, -5))
        try {
            Totp.hotp("12345678901234567890".toByteArray(), 0, 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun crashHardeningEdges() {
        try {
            Totp.base32Decode("   ")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Totp.base32Decode("====")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Totp.base32Decode("ABC$%^")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertArrayEquals(
            "12345678901234567890".toByteArray(Charsets.UTF_8),
            Totp.base32Decode("gezdgnbvgy3tqojqgezdgnbvgy3tqojq")
        )
        assertArrayEquals(
            byteArrayOf('f'.code.toByte()),
            Totp.base32Decode("MY")
        )
        assertEquals("287082", Totp.totp("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ====", 59))
        try {
            Totp.hotp(ByteArray(0), 0)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Totp.hotp("12345678901234567890".toByteArray(), 0, 10)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Totp.totp(sha1Secret, 59, -30)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals(6, Totp.hotp("12345678901234567890".toByteArray(), 1, 6).length)
        assertEquals(30L, Totp.secondsRemaining(0, 30))
    }
}
