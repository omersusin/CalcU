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
}
