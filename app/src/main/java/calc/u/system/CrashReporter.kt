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

    @Volatile
    private var installed = false

    fun install(context: Context) {
        runCatching {
            if (installed) return
            installed = true
            val appContext = context.applicationContext
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                runCatching { writeCrash(appContext, throwable) }
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

    private fun writeCrash(context: Context, throwable: Throwable) {
        runCatching {
            val dir = File(context.filesDir, DIR_NAME)
            runCatching { if (!dir.exists()) dir.mkdirs() }.getOrDefault(false)
            val timestamp = runCatching {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            }.getOrDefault("unknown")
            val version = runCatching { appVersion(context) }.getOrDefault("unknown")
            val sdk = runCatching { "${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})" }.getOrDefault("unknown")
            val stacktrace = runCatching { stacktraceOf(throwable) }.getOrDefault(throwable.toString())
            val content = buildString {
                appendLine("Timestamp: $timestamp")
                appendLine("App version: $version")
                appendLine("Android SDK: $sdk")
                appendLine("Stacktrace:")
                appendLine(stacktrace)
            }
            runCatching {
                val target = File(dir, FILE_NAME)
                target.writeText(content)
            }
            runCatching {
                dir.listFiles()?.forEach { file ->
                    if (file.name != FILE_NAME) runCatching { file.delete() }
                }
            }
        }
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
