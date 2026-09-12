package calc.u.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import calc.u.R
import calc.u.core.Engine

class CalcUKeypadWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        runCatching {
            val appContext = runCatching { context.applicationContext }.getOrNull() ?: context
            appWidgetIds.forEach { id ->
                runCatching { render(appContext, id) }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        runCatching {
            if (intent.action == ACTION_KEY) {
                val appWidgetId = runCatching {
                    intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID
                    )
                }.getOrDefault(AppWidgetManager.INVALID_APPWIDGET_ID)
                val key = runCatching { intent.getStringExtra(EXTRA_KEY) }.getOrNull().orEmpty()
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && key.isNotEmpty()) {
                    val appContext = runCatching { context.applicationContext }.getOrNull() ?: context
                    runCatching { handleKey(appContext, appWidgetId, key) }
                    runCatching { render(appContext, appWidgetId) }
                }
            } else {
                super.onReceive(context, intent)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        runCatching {
            val appContext = runCatching { context.applicationContext }.getOrNull() ?: context
            appWidgetIds.forEach { id ->
                runCatching {
                    appContext.getSharedPreferences(prefsName(id), Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                }
            }
        }
    }

    companion object {
        const val ACTION_KEY = "calc.u.KEY"
        const val EXTRA_KEY = "key"

        private const val KEY_EXPRESSION = "expression"
        private const val KEY_FRESH = "fresh_result"
        private const val MAX_LEN = 200

        private val KEYS: List<Pair<Int, String>> = listOf(
            R.id.key_clear to "C",
            R.id.key_back to "⌫",
            R.id.key_div to "÷",
            R.id.key_mul to "×",
            R.id.key_7 to "7",
            R.id.key_8 to "8",
            R.id.key_9 to "9",
            R.id.key_minus to "-",
            R.id.key_4 to "4",
            R.id.key_5 to "5",
            R.id.key_6 to "6",
            R.id.key_plus to "+",
            R.id.key_1 to "1",
            R.id.key_2 to "2",
            R.id.key_3 to "3",
            R.id.key_eq to "=",
            R.id.key_0 to "0",
            R.id.key_dot to "."
        )

        private fun prefsName(appWidgetId: Int): String = "keypad_widget_$appWidgetId"

        private fun readExpression(appContext: Context, appWidgetId: Int): String {
            return runCatching {
                appContext.getSharedPreferences(prefsName(appWidgetId), Context.MODE_PRIVATE)
                    .getString(KEY_EXPRESSION, "")
            }.getOrNull().orEmpty()
        }

        private fun handleKey(appContext: Context, appWidgetId: Int, key: String) {
            runCatching {
                val prefs = appContext.getSharedPreferences(prefsName(appWidgetId), Context.MODE_PRIVATE)
                var expr = runCatching { prefs.getString(KEY_EXPRESSION, "") }.getOrNull().orEmpty()
                if (expr == "Error") expr = ""
                val fresh = runCatching { prefs.getBoolean(KEY_FRESH, false) }.getOrDefault(false)

                var next = expr
                var nextFresh = false
                when (key) {
                    "C" -> next = ""
                    "⌫" -> next = expr.dropLast(1)
                    "=" -> {
                        if (expr.isNotBlank()) {
                            val result = Engine.eval(expr)
                            next = if (result.isSuccess) {
                                runCatching { Engine.format(result.getOrThrow()) }.getOrNull() ?: "Error"
                            } else {
                                "Error"
                            }
                            nextFresh = next != "Error"
                        }
                    }
                    else -> {
                        next = if (fresh && (key[0].isDigit() || key == ".")) {
                            key
                        } else if (expr.length >= MAX_LEN) {
                            expr
                        } else {
                            expr + key
                        }
                    }
                }

                runCatching {
                    prefs.edit()
                        .putString(KEY_EXPRESSION, next)
                        .putBoolean(KEY_FRESH, nextFresh)
                        .apply()
                }
            }
        }

        private fun render(appContext: Context, appWidgetId: Int) {
            runCatching {
                val manager = runCatching { AppWidgetManager.getInstance(appContext) }.getOrNull()
                    ?: return@runCatching
                val views = RemoteViews(appContext.packageName, R.layout.widget_keypad)
                val expr = readExpression(appContext, appWidgetId)
                runCatching {
                    views.setTextViewText(R.id.keypad_display, expr.ifEmpty { "0" })
                }
                KEYS.forEachIndexed { index, (viewId, key) ->
                    runCatching {
                        val intent = Intent(appContext, CalcUKeypadWidget::class.java)
                            .setAction(ACTION_KEY)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            .putExtra(EXTRA_KEY, key)
                        val pending = PendingIntent.getBroadcast(
                            appContext,
                            appWidgetId * 64 + index,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(viewId, pending)
                    }
                }
                runCatching { manager.updateAppWidget(appWidgetId, views) }
            }
        }
    }
}
