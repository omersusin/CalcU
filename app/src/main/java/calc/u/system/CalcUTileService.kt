package calc.u.system

import android.content.Intent
import android.service.quicksettings.TileService
import calc.u.MainActivity

class CalcUTileService : TileService() {
    override fun onClick() {
        super.onClick()
        unlockAndRun {
            startActivityAndCollapse(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
