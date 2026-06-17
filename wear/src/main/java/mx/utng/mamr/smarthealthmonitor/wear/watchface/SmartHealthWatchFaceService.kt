package mx.utng.mamr.smarthealthmonitor.wear.watchface

import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.*
import java.time.ZonedDateTime

class SmartHealthWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = SmartHealthRenderer(
            context                    = applicationContext,
            surfaceHolder              = surfaceHolder,
            watchState                 = watchState,
            complicationSlotsManager   = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository,
            interactiveDrawModeUpdateDelayMillis = 1_000L
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer)
    }
}
