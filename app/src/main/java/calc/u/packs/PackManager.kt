package calc.u.packs

import android.content.res.AssetManager
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request

fun okHttpDownload(url: String): ByteArray? = runCatching {
    require(url.startsWith("https://")) { "only https pack urls" }
    val client = OkHttpClient()
    val req = Request.Builder().url(url).get().build()
    client.newCall(req).execute().use { resp ->
        require(resp.isSuccessful) { "http ${resp.code}" }
        resp.body?.bytes()
    }
}.getOrNull()

class PackManager(
    private val filesDir: File,
    private val assets: AssetManager? = null,
    private val download: (String) -> ByteArray? = ::okHttpDownload
) {
    private fun dir(id: String): File {
        require(validPackId(id)) { "bad pack id" }
        return File(filesDir, "packs/$id")
    }

    private fun metaFile(): File = File(filesDir, "packs/enabled.json")

    fun installed(): List<PackManifest> = runCatching {
        val root = File(filesDir, "packs")
        (root.listFiles() ?: emptyArray())
            .filter { it.isDirectory }
            .mapNotNull { d ->
                runCatching { parseManifest(File(d, "manifest.json").readText()) }.getOrNull()
            }
            .sortedBy { it.name }
    }.getOrDefault(emptyList())

    fun entrySource(id: String): String? = runCatching {
        val manifest = parseManifest(File(dir(id), "manifest.json").readText())
        require(validEntryName(manifest.entry)) { "bad entry name" }
        File(dir(id), manifest.entry).readText()
    }.getOrNull()

    fun enabledIds(): Set<String> = runCatching {
        val f = metaFile()
        if (!f.exists()) return emptySet()
        decodeEnabled(f.readText())
    }.getOrDefault(emptySet())

    fun setEnabled(id: String, on: Boolean) {
        runCatching {
            require(validPackId(id)) { "bad pack id" }
            val next = enabledIds().toMutableSet()
            if (on) next.add(id) else next.remove(id)
            metaFile().parentFile?.mkdirs()
            metaFile().writeText(encodeEnabled(next))
        }
    }

    fun installPack(packBytes: ByteArray): Result<PackManifest> = runCatching {
        val file = packJson.decodeFromString<PackFile>(packBytes.decodeToString())
        val m = file.manifest
        require(validPackId(m.id)) { "bad pack id" }
        require(m.version > 0) { "bad version" }
        require(m.runtime == RUNTIME_ID) { "unsupported runtime ${m.runtime}" }
        require(validEntryName(m.entry)) { "bad entry name" }
        val entryBytes = file.entry.toByteArray(Charsets.UTF_8)
        require(verifyEntry(entryBytes, m.sha256)) { "entry hash mismatch" }
        val d = dir(m.id)
        d.mkdirs()
        File(d, "manifest.json").writeText(encodeManifest(m))
        File(d, m.entry).writeBytes(entryBytes)
        setEnabled(m.id, true)
        m
    }

    fun installFromCatalog(entry: CatalogEntry): Result<PackManifest> = runCatching {
        val bytes: ByteArray = if (entry.url.startsWith("asset://")) {
            val am = assets ?: error("no assets")
            am.open(entry.url.removePrefix("asset://")).use { it.readBytes() }
        } else {
            download(entry.url) ?: error("download failed")
        }
        require(verifyEntry(bytes, entry.sha256)) { "pack hash mismatch" }
        installPack(bytes).getOrThrow()
    }

    fun remove(id: String) {
        runCatching {
            require(validPackId(id)) { "bad pack id" }
            dir(id).deleteRecursively()
            setEnabled(id, false)
        }
    }
}
