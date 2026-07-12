package mx.utng.mamr.smarthealthmonitor.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.mamr.smarthealthmonitor.mqtt.TvMessage
import mx.utng.mamr.smarthealthmonitor.tv.mqtt.MqttTvSubscriber

class TvViewModel(application: Application) : AndroidViewModel(application) {

    // Flow de mensajes MQTT entrantes
    private val mqttFlow = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(application, mqttFlow)

    init {
        mqttSubscriber.connect()
        // Observar mensajes MQTT y actualizar el estado del repositorio local de la TV
        viewModelScope.launch {
            mqttFlow.collect { tvMsg ->
                tvMsg ?: return@collect
                SmartHealthRepository.actualizarFC(tvMsg.bpm)
            }
        }
    }

    // FC actual del wearable (o 0 si no hay dato)
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    // Pasos actuales del wearable (o 0 si no hay dato)
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    // Historial de lecturas desde Room DAO
    val historial: StateFlow<List<LecturaFC>> = SmartHealthRepository.obtenerHistorial()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
    }
}
