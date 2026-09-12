package calc.u

import android.app.Application
import calc.u.system.CrashReporter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CalcUApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
    }
}
