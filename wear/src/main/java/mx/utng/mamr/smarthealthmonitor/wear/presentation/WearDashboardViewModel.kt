package mx.utng.mamr.smarthealthmonitor.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.mamr.smarthealthmonitor.wear.mqtt.MqttWearPublisher
import mx.utng.mamr.smarthealthmonitor.wear.WearDataSender

class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val mqttPublisher = MqttWearPublisher(application)
    private val wearDataSender = WearDataSender(application)

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            mqttPublisher.connect()
        }
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { bpm ->
                if (bpm > 0) {
                    val estado = when {
                        bpm < 60 -> "FC Baja"
                        bpm > 100 -> "FC Alta"
                        else -> "Normal"
                    }
                    mqttPublisher.publishFC(bpm, estado)
                }
            }
        }
    }

    // Definimos fc obteniendo los datos del repositorio
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) 72 else it } // Valor por defecto si es 0
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            72
        )

    // Historial desde Room (repositorio compartido)
    val historial: StateFlow<List<LecturaFC>> = SmartHealthRepository.obtenerHistorial()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun simularMedicion(bpm: Int) {
        viewModelScope.launch {
            SmartHealthRepository.actualizarFC(bpm)
            try {
                wearDataSender.enviarFC(bpm)
                android.util.Log.d("WearVM", "Simulado: $bpm bpm enviado al teléfono")
            } catch (e: Exception) {
                android.util.Log.e("WearVM", "Error al enviar simulación: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttPublisher.disconnect()
    }
}
