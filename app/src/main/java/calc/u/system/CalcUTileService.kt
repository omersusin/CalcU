package calc.u.system

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.TileService
import calc.u.MainActivity

class CalcUTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val launch = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending = PendingIntent.getActivity(this, 0, launch, PendingIntent.FLAG_IMMUTABLE)
        unlockAndRun { startActivityAndCollapse(pending) }
    }
}
