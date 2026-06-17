package mx.utng.mamr.smarthealthmonitor.wear.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SmartHealthRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    interactiveDrawModeUpdateDelayMillis: Long
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder, currentUserStyleRepository, watchState,
    CanvasType.SOFTWARE, interactiveDrawModeUpdateDelayMillis,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = true
) {

    private val paintHora = Paint().apply {
        color     = Color.WHITE
        textSize  = 72f
        isAntiAlias = true
        typeface  = Typeface.DEFAULT_BOLD
    }
    private val paintFC = Paint().apply {
        color     = Color.RED
        textSize  = 30f
        isAntiAlias = true
    }
    private val paintSub = Paint().apply {
        color     = Color.GRAY; textSize = 22f; isAntiAlias = true
    }

    override suspend fun createSharedAssets(): SharedAssets =
        object : SharedAssets { override fun onDestroy() {} }

    override fun render(canvas: Canvas, bounds: Rect,
                        zonedDateTime: ZonedDateTime,
                        sharedAssets: SharedAssets) {
        Log.d("SmartHealthRenderer", "Rendering WatchFace at ${zonedDateTime.hour}:${zonedDateTime.minute}:${zonedDateTime.second}")

        // Fondo negro — ahorra batería en modo AOD
        canvas.drawColor(Color.BLACK)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        // Hora digital centrada
        val hora = String.format("%02d:%02d",
            zonedDateTime.hour, zonedDateTime.minute)
        val tw = paintHora.measureText(hora)
        canvas.drawText(hora, cx - tw/2, cy - 10f, paintHora)

        // Segundos (pequeño debajo)
        val seg = String.format("%02d", zonedDateTime.second)
        canvas.drawText(seg, cx - 18f, cy + 30f, paintSub)

        // FC desde SmartHealthRepository
        val fc = SmartHealthRepository.fcFlow.value
        if (fc > 0) {
            val fcStr = "❤ $fc bpm"
            val fcW = paintFC.measureText(fcStr)
            canvas.drawText(fcStr, cx - fcW/2, cy + 70f, paintFC)
        }
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect,
                                      zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)
    }
}

