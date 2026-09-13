package calc.u.system

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashReporter {
    private const val DIR_NAME = "crashes"
    private const val FILE_NAME = "last.txt"
    private const val MAX_HISTORY = 6

    @Volatile
    private var installed = false

    fun install(context: Context) {
        runCatching {
            if (installed) return
            installed = true
            val appContext = context.applicationContext
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                runCatching { writeCrash(appContext, thread, throwable) }
                if (previous != null) {
                    runCatching { previous.uncaughtException(thread, throwable) }
                }
            }
        }
    }

    fun readLast(context: Context): String? {
        return runCatching {
            val file = crashFile(context.applicationContext)
            if (!file.exists()) return null
            val text = file.readText()
            if (text.isBlank()) null else text
        }.getOrNull()
    }

    fun clear(context: Context) {
        runCatching {
            val file = crashFile(context.applicationContext)
            if (file.exists()) file.delete()
        }
    }

    private fun crashFile(context: Context): File {
        return File(File(context.filesDir, DIR_NAME), FILE_NAME)
    }

    private fun writeCrash(context: Context, thread: Thread, throwable: Throwable) {
        runCatching {
            val dir = File(context.filesDir, DIR_NAME)
            runCatching { if (!dir.exists()) dir.mkdirs() }.getOrDefault(false)
            val now = runCatching {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            }.getOrDefault("unknown")
            val version = runCatching { appVersion(context) }.getOrDefault("unknown")
            val sdk = runCatching { "${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})" }.getOrDefault("unknown")
            val stacktrace = runCatching { stacktraceOf(throwable) }.getOrDefault(throwable.toString())
            val cause = runCatching { causeChain(throwable) }.getOrDefault("")
            val content = buildString {
                appendLine("Timestamp: $now")
                appendLine("App version: $version")
                appendLine("Android SDK: $sdk")
                appendLine("Thread: ${thread.name}")
                appendLine("Type: ${throwable.javaClass.name}")
                throwable.message?.let { appendLine("Message: $it") }
                appendLine("Stacktrace:")
                appendLine(stacktrace)
                if (cause.isNotBlank()) {
                    appendLine("Cause chain:")
                    appendLine(cause)
                }
            }
            runCatching {
                val target = File(dir, FILE_NAME)
                target.writeText(content)
            }
            runCatching {
                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
                val archive = File(dir, "crash_$stamp.txt")
                runCatching { archive.writeText(content) }
            }
            runCatching {
                val archived = dir.listFiles()?.filter { it.name.startsWith("crash_") }?.sortedByDescending { it.lastModified() }.orEmpty()
                archived.drop(MAX_HISTORY).forEach { file -> file.delete() }
            }
        }
    }

    private fun causeChain(throwable: Throwable): String {
        return runCatching {
            val out = StringBuilder()
            var cur = throwable.cause
            var depth = 0
            while (cur != null && depth < 8) {
                out.append("Caused by ${cur.javaClass.name}: ${cur.message}\n")
                cur = cur.cause
                depth++
            }
            out.toString().trimEnd()
        }.getOrDefault("")
    }

    private fun appVersion(context: Context): String {
        return runCatching {
            @Suppress("DEPRECATION")
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val name = info.versionName ?: "unknown"
            val code = runCatching {
                if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
            }.getOrDefault(-1L)
            "$name ($code)"
        }.getOrDefault("unknown")
    }

    private fun stacktraceOf(throwable: Throwable): String {
        return runCatching {
            val writer = StringWriter()
            PrintWriter(writer).use { printer -> throwable.printStackTrace(printer) }
            writer.toString()
        }.getOrDefault(throwable.toString())
    }
}
