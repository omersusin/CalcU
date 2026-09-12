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
}
