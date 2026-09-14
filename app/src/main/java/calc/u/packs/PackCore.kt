package calc.u.packs

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.security.MessageDigest

const val RUNTIME_ID = "duktape-js-1"

@Serializable
data class PackTool(val id: String, val label: String, val function: String, val hint: String = "")

@Serializable
data class PackManifest(
    val id: String,
    val version: Int,
    val name: String,
    val description: String = "",
    val runtime: String = RUNTIME_ID,
    val entry: String = "main.js",
    val sha256: String,
    val tools: List<PackTool> = emptyList()
)

@Serializable
data class PackFile(val manifest: PackManifest, val entry: String)

@Serializable
data class CatalogEntry(
    val id: String,
    val version: Int,
    val name: String,
    val description: String = "",
    val url: String,
    val sha256: String
)

@Serializable
data class PackCatalog(val packs: List<CatalogEntry> = emptyList())

internal val packJson = Json { ignoreUnknownKeys = true }

fun parseManifest(raw: String): PackManifest = packJson.decodeFromString(raw)

fun encodeManifest(m: PackManifest): String =
    packJson.encodeToString(PackManifest.serializer(), m)

fun parseCatalog(raw: String): PackCatalog =
    runCatching { packJson.decodeFromString<PackCatalog>(raw) }.getOrDefault(PackCatalog())

fun encodeEnabled(ids: Set<String>): String =
    packJson.encodeToString(SetSerializer(serializer<String>()), ids)

fun decodeEnabled(raw: String): Set<String> =
    runCatching { packJson.decodeFromString<Set<String>>(raw) }.getOrDefault(emptySet())

fun sha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}

fun verifyEntry(bytes: ByteArray, expectedHex: String): Boolean =
    expectedHex.isNotBlank() && sha256Hex(bytes).equals(expectedHex, ignoreCase = true)

fun validPackId(id: String): Boolean =
    id.length in 1..32 && id.matches(Regex("[a-z0-9-]+"))

fun validEntryName(name: String): Boolean =
    name.length in 1..64 && !name.contains("..") &&
        name.matches(Regex("[A-Za-z0-9_.-]+"))

fun jsQuote(s: String): String {
    val sb = StringBuilder("\"")
    s.forEach { c ->
        when (c) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> if (c < ' ') sb.append("\\u%04x".format(c.code)) else sb.append(c)
        }
    }
    sb.append('"')
    return sb.toString()
}

fun jsCall(function: String, input: String): String {
    require(function.matches(Regex("[A-Za-z_$][A-Za-z0-9_$]*"))) { "bad function name" }
    return "$function(${jsQuote(input)})"
}
