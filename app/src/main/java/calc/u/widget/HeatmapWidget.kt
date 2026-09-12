package calc.u.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import calc.u.MainActivity
import calc.u.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val Context.heatmapStore by preferencesDataStore("calcu")

class HeatmapWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = runCatching { goAsync() }.getOrNull()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                renderAll(context, appWidgetManager, appWidgetIds)
            } catch (_: Exception) {
            } finally {
                runCatching { pending?.finish() }
            }
        }
    }

    private suspend fun renderAll(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val counts = runCatching { loadCounts(context) }.getOrDefault(emptyMap())
        val bitmap = runCatching { drawHeatmap(counts) }.getOrNull()
        val total = runCatching { counts.values.sum() }.getOrDefault(0)
        for (id in ids) {
            runCatching {
                val views = RemoteViews(context.packageName, R.layout.widget_heatmap)
                if (bitmap != null) {
                    runCatching { views.setImageViewBitmap(R.id.heatmap_image, bitmap) }
                }
                runCatching { views.setTextViewText(R.id.heatmap_count, "$total in 70d") }
                val open = Intent(context, MainActivity::class.java)
                val pi = PendingIntent.getActivity(
                    context,
                    id,
                    open,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                views.setOnClickPendingIntent(R.id.heatmap_root, pi)
                manager.updateAppWidget(id, views)
            }
        }
    }

    private suspend fun loadCounts(context: Context): Map<Long, Int> {
        val prefs = context.heatmapStore.data.first()
        val raw = runCatching { prefs[stringPreferencesKey("history")] }.getOrNull() ?: "[]"
        val list = try {
            Json.decodeFromString<List<String>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
        val today = runCatching { System.currentTimeMillis() / 86400000L }.getOrDefault(0L)
        val counts = mutableMapOf<Long, Int>()
        for (e in list) {
            val ts = runCatching { e.substringBefore("|").toLong() }.getOrNull() ?: continue
            val day = ts / 86400000L
            if (day in (today - 69)..today) {
                counts[day] = (counts[day] ?: 0) + 1
            }
        }
        return counts.toMap()
    }

    private fun drawHeatmap(counts: Map<Long, Int>): Bitmap {
        val w = 1000
        val h = 440
        val cols = 10
        val rows = 7
        val pad = 24f
        val gap = 12f
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = runCatching { Canvas(bmp) }.getOrNull() ?: return bmp
        val today = runCatching { System.currentTimeMillis() / 86400000L }.getOrDefault(0L)
        val max = runCatching { counts.values.maxOrNull() ?: 0 }.getOrDefault(0)
        val cellW = (w - pad * 2f - gap * (cols - 1)) / cols
        val cellH = (h - pad * 2f - gap * (rows - 1)) / rows
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.parseColor("#6750A4")
        }
        val rect = RectF()
        for (i in 0 until cols * rows) {
            val day = today - (cols * rows - 1 - i)
            val col = i / rows
            val row = i % rows
            val count = counts[day] ?: 0
            val left = pad + col * (cellW + gap)
            val top = pad + row * (cellH + gap)
            rect.set(left, top, left + cellW, top + cellH)
            runCatching {
                if (count > 0) {
                    val frac = if (max > 0) count.toFloat() / max.toFloat() else 0f
                    fill.style = Paint.Style.FILL
                    fill.color = Color.parseColor("#6750A4")
                    fill.alpha = (90 + (frac * 165f)).toInt().coerceIn(0, 255)
                } else {
                    fill.style = Paint.Style.FILL
                    fill.color = Color.parseColor("#E2E0EC")
                    fill.alpha = 255
                }
                canvas.drawRoundRect(rect, 14f, 14f, fill)
                if (day == today) {
                    canvas.drawRoundRect(rect, 14f, 14f, stroke)
                }
            }
        }
        return bmp
    }
}
