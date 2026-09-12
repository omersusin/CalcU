package calc.u.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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

private val Context.widgetStore by preferencesDataStore("calcu")

class CalcUWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = runCatching { goAsync() }.getOrNull()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.widgetStore.data.first()
                val raw = runCatching { prefs[stringPreferencesKey("history")] }.getOrNull() ?: "[]"
                val list = try {
                    Json.decodeFromString<List<String>>(raw)
                } catch (_: Exception) {
                    emptyList()
                }
                val text = runCatching {
                    list.firstOrNull()?.split("|")?.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "CalcU"
                }.getOrDefault("CalcU")
                for (id in appWidgetIds) {
                    runCatching {
                        val views = RemoteViews(context.packageName, R.layout.widget_calcu)
                        views.setTextViewText(R.id.widget_result, text)
                        val open = Intent(context, MainActivity::class.java)
                        val pi = PendingIntent.getActivity(
                            context,
                            0,
                            open,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_root, pi)
                        appWidgetManager.updateAppWidget(id, views)
                    }
                }
            } catch (_: Exception) {
            } finally {
                runCatching { pending?.finish() }
            }
        }
    }
}
