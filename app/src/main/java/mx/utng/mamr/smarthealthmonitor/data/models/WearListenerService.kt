package mx.utng.mamr.smarthealthmonitor.data.models

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import mx.utng.mamr.smarthealthmonitor.SmartHealthApplication

class WearListenerService : WearableListenerService() {

    companion object {
        const val PATH_FC    = "/smarthealthmonitor/fc"
        const val PATH_PASOS = "/smarthealthmonitor/pasos"
        private const val TAG = "WearListener"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val data   = String(messageEvent.data)
        val path   = messageEvent.path
        Log.d(TAG, "Mensaje recibido: path=$path, data=$data")

        when (path) {
            PATH_FC -> {
                val bpm = data.toIntOrNull() ?: return
                SmartHealthRepository.actualizarFC(bpm)
                
                // Puentear el dato al broker MQTT para el TV
                val estado = when {
                    bpm < 60 -> "FC Baja"
                    bpm > 100 -> "FC Alta"
                    else -> "Normal"
                }
                (applicationContext as? SmartHealthApplication)?.mqttService?.publishFcToTv(bpm, estado)
            }
            PATH_PASOS -> {
                val pasos = data.toIntOrNull() ?: return
                SmartHealthRepository.actualizarPasos(pasos)
            }
            else -> Log.w(TAG, "Path desconocido: $path")
        }
    }
}