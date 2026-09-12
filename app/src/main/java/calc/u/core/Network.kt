package calc.u.core

data class Subnet(
    val network: String,
    val broadcast: String,
    val mask: String,
    val hosts: Long,
    val firstHost: String,
    val lastHost: String
)

object Network {
    private const val MASK_ALL = 0xFFFFFFFFL

    fun ipToLong(ip: String): Long {
        val parts = ip.trim().split(".")
        require(parts.size == 4) { "invalid IP: $ip" }
        var v = 0L
        for (p in parts) {
            val o = p.toIntOrNull() ?: throw IllegalArgumentException("invalid IP: $ip")
            require(o in 0..255) { "invalid IP: $ip" }
            v = (v shl 8) or o.toLong()
        }
        return v
    }

    fun ipToString(value: Long): String {
        require(value in 0..MASK_ALL) { "value out of IPv4 range" }
        val a = (value shr 24) and 0xFF
        val b = (value shr 16) and 0xFF
        val c = (value shr 8) and 0xFF
        val d = value and 0xFF
        return "$a.$b.$c.$d"
    }

    fun subnet(ip: String, prefix: Int): Subnet {
        require(prefix in 0..32) { "prefix must be in 0..32" }
        val ipLong = ipToLong(ip)
        val maskLong = if (prefix == 0) 0L else (MASK_ALL shl (32 - prefix)) and MASK_ALL
        val networkLong = ipLong and maskLong
        val broadcastLong = networkLong or (maskLong.inv() and MASK_ALL)
        val hosts: Long = when (prefix) {
            32 -> 1L
            31 -> 2L
            else -> (1L shl (32 - prefix)) - 2
        }
        val first = when (prefix) {
            32, 31 -> networkLong
            else -> networkLong + 1
        }
        val last = when (prefix) {
            32 -> networkLong
            31 -> broadcastLong
            else -> broadcastLong - 1
        }
        return Subnet(
            network = ipToString(networkLong),
            broadcast = ipToString(broadcastLong),
            mask = ipToString(maskLong),
            hosts = hosts,
            firstHost = ipToString(first),
            lastHost = ipToString(last)
        )
    }
}
