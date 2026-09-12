package calc.u

import calc.u.core.Network
import org.junit.Assert.*
import org.junit.Test

class NetworkTest {
    @Test fun slash24() {
        val s = Network.subnet("192.168.1.10", 24)
        assertEquals("192.168.1.0", s.network)
        assertEquals("192.168.1.255", s.broadcast)
        assertEquals("255.255.255.0", s.mask)
        assertEquals(254L, s.hosts)
        assertEquals("192.168.1.1", s.firstHost)
        assertEquals("192.168.1.254", s.lastHost)
    }
    @Test fun slash32SingleHost() {
        val s = Network.subnet("10.0.0.5", 32)
        assertEquals("10.0.0.5", s.network)
        assertEquals("10.0.0.5", s.broadcast)
        assertEquals(1L, s.hosts)
        assertEquals("10.0.0.5", s.firstHost)
        assertEquals("10.0.0.5", s.lastHost)
    }
    @Test fun invalidIpThrows() {
        try {
            Network.subnet("999.1.1.1", 24)
            fail("invalid IP should throw")
        } catch (e: IllegalArgumentException) { }
    }
    @Test fun roundTrip() {
        assertEquals(0xC0A8010AL, Network.ipToLong("192.168.1.10"))
        assertEquals("192.168.1.10", Network.ipToString(0xC0A8010AL))
    }

    @Test fun invalidIpEdgeCases() {
        try {
            Network.ipToLong("")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("1.2.3")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("1.2.3.256")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("a.b.c.d")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.subnet("192.168.1.1", 33)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToString(-1L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
    }

    @Test fun crashHardeningEdges() {
        try {
            Network.ipToLong("1.2.3.4.5")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("   ")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("1.2.3.-1")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("1.2.3.99999999999999999999")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("0x7f.0.0.1")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToLong("1..3.4")
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.ipToString(0x1_0000_0000L)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        try {
            Network.subnet("192.168.1.1", -1)
            fail("expected IAE")
        } catch (e: IllegalArgumentException) { }
        assertEquals("0.0.0.0", Network.ipToString(0L))
        assertEquals("255.255.255.255", Network.ipToString(0xFFFFFFFFL))
        assertEquals(2L, Network.subnet("192.168.1.0", 31).hosts)
        assertEquals(4294967294L, Network.subnet("0.0.0.0", 0).hosts)
    }
}
