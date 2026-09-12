package calc.u

import calc.u.core.TextData
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class CryptoTest {
    private fun hexToBytes(hex: String): ByteArray {
        val t = hex.trim()
        require(t.length % 2 == 0)
        return ByteArray(t.length / 2) { i ->
            ((t[i * 2].digitToInt(16) shl 4) or t[i * 2 + 1].digitToInt(16)).toByte()
        }
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    @Test fun hmacSha256Rfc4231() {
        val key = "\u000b".repeat(20)
        assertEquals(
            "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9bda7132aa",
            TextData.hmacSha256(key, "Hi There")
        )
    }

    @Test fun hmacLengths() {
        assertEquals(96, TextData.hmacSha384("k", "m").length)
        assertEquals(128, TextData.hmacSha512("k", "m").length)
    }

    @Test fun jwtRoundTrip() {
        val enc = java.util.Base64.getUrlEncoder().withoutPadding()
        val h = enc.encodeToString("{\"alg\":\"none\"}".toByteArray(Charsets.UTF_8))
        val p = enc.encodeToString("{\"sub\":\"123\"}".toByteArray(Charsets.UTF_8))
        val (header, payload, sig) = TextData.jwtDecode("$h.$p.sig-part")
        assertEquals("{\"alg\":\"none\"}", header)
        assertEquals("{\"sub\":\"123\"}", payload)
        assertTrue(sig.contains("sig-part"))
    }

    @Test fun jwtBearerPrefix() {
        val enc = java.util.Base64.getUrlEncoder().withoutPadding()
        val h = enc.encodeToString("{}".toByteArray(Charsets.UTF_8))
        val p = enc.encodeToString("{}".toByteArray(Charsets.UTF_8))
        val (header, _, _) = TextData.jwtDecode("Bearer $h.$p.s")
        assertEquals("{}", header)
    }

    @Test(expected = IllegalArgumentException::class)
    fun jwtInvalidThrows() {
        TextData.jwtDecode("a.b.c")
    }

    @Test fun base32Foo() {
        assertEquals("MFRGG===", TextData.base32Encode("foo"))
        assertEquals("foo", TextData.base32Decode("MFRGG==="))
        assertEquals("", TextData.base32Encode(""))
        assertEquals("", TextData.base32Decode("   "))
    }

    @Test fun base58RoundTrip() {
        val enc = TextData.base58Encode("hello")
        assertEquals("hello", TextData.base58Decode(enc))
        assertEquals("", TextData.base58Encode(""))
        val leading = TextData.base58Encode("\u0000A")
        assertTrue(leading.startsWith("1"))
        assertEquals("\u0000A", TextData.base58Decode(leading))
    }

    @Test fun htmlRoundTrip() {
        assertEquals("&lt;b&gt;", TextData.htmlEscape("<b>"))
        val original = "<div class=\"a\">&'test'</div>"
        assertEquals(original, TextData.htmlUnescape(TextData.htmlEscape(original)))
        assertEquals("<&>'\"", TextData.htmlUnescape("&lt;&amp;&gt;&#39;&quot;"))
        assertEquals("A", TextData.htmlUnescape("&#65;"))
        assertEquals("A", TextData.htmlUnescape("&#x41;"))
    }

    @Test fun checksums() {
        assertEquals(0xCBF43926L, TextData.crc32("123456789"))
        assertEquals(0xBB3D, TextData.crc16("123456789"))
        assertEquals(0x091E01DEL, TextData.adler32("123456789"))
    }

    @Test fun shaVectors() {
        assertEquals(
            "a9993e364706816aba3e25717850c26c9cd0d4d",
            TextData.sha1("abc")
        )
        assertEquals(
            "a7ffc6f8bb1ed2643477c6d4f73f14f7215f9d43a2d1ed2d5ad7835f2d177a8b",
            TextData.sha3_256("abc")
        )
        assertEquals(56, TextData.sha224("abc").length)
        assertEquals(96, TextData.sha3_384("abc").length)
        assertEquals(128, TextData.sha3_512("abc").length)
    }

    @Test fun pbkdf2Smoke() {
        val dk = TextData.pbkdf2Sha256("password", "73616c74", 1, 256)
        assertEquals(64, dk.length)
    }

    @Test fun hkdfRfc5869Case1() {
        val ikm = ByteArray(22) { 0x0b }
        val salt = hexToBytes("000102030405060708090a0b0c")
        val info = hexToBytes("f0f1f2f3f4f5f6f7f8f9")
        val okm = TextData.hkdfSha256(ikm, salt, info, 42)
        assertEquals(42, okm.size)
        assertTrue(bytesToHex(okm).startsWith("3cb25f25"))
    }

    @Test fun aesRoundTrip() {
        val key = "00112233445566778899aabbccddeeff"
        val iv = "0102030405060708090a0b0c0d0e0f10"
        val ct = TextData.aesCbcEncrypt(key, iv, "hello AES")
        assertEquals("hello AES", TextData.aesCbcDecrypt(key, iv, ct))
    }

    @Test fun chachaRoundTrip() {
        val key = "00".repeat(32)
        val nonce = "00".repeat(12)
        val ct = TextData.chacha20Encrypt(key, nonce, 0, "hello chacha")
        assertEquals("hello chacha", TextData.chacha20Decrypt(key, nonce, 0, ct))
    }

    @Test fun rc4RoundTrip() {
        val ct = TextData.rc4("hello rc4", "key123")
        assertEquals("hello rc4", TextData.rc4Decrypt(ct, "key123"))
    }

    @Test fun cborRoundTrip() {
        val hex = TextData.cborEncodeJson("{\"a\":1}")
        assertEquals("{\"a\":1}", TextData.cborDecodeHex(hex))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cborFloatUnsupported() {
        TextData.cborEncodeJson("1.5")
    }

    @Test fun uuidVersions() {
        assertEquals(1, UUID.fromString(TextData.uuidV1()).version())
        assertEquals(6, UUID.fromString(TextData.uuidV6()).version())
        assertEquals(7, UUID.fromString(TextData.uuidV7()).version())
    }

    @Test fun bcryptRoundTrip() {
        val hash = TextData.bcryptHash("secret", 4)
        assertTrue(TextData.bcryptVerify("secret", hash))
        assertFalse(TextData.bcryptVerify("wrong", hash))
    }
}
