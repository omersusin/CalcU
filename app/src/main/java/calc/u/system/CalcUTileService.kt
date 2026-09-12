package calc.u.system

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import calc.u.MainActivity

class CalcUTileService : TileService() {
    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val launch = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        unlockAndRun {
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(this, 0, launch, PendingIntent.FLAG_IMMUTABLE)
                )
            } else {
                startActivityAndCollapse(launch)
            }
        }
    }
}
