package mx.utng.mamr.smarthealthmonitor.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.mamr.smarthealthmonitor.mqtt.TvMessage
import mx.utng.mamr.smarthealthmonitor.tv.mqtt.MqttTvSubscriber

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val neonRepo = mx.utng.mamr.smarthealthmonitor.tv.data.TvNeonRepository()

    // Flow de mensajes MQTT entrantes
    private val mqttFlow = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(application, mqttFlow)

    private val _historial = MutableStateFlow<List<LecturaFC>>(emptyList())
    val historial: StateFlow<List<LecturaFC>> = _historial.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mqttSubscriber.connect()
        }
        // Observar mensajes MQTT y actualizar el estado
        viewModelScope.launch {
            mqttFlow.collect { tvMsg ->
                tvMsg ?: return@collect
                SmartHealthRepository.actualizarFC(tvMsg.bpm)
                cargarDatos()
            }
        }
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                val lecturasDto = neonRepo.obtenerHistorialCompleto(50)
                val lecturas = lecturasDto.map { dto ->
                    LecturaFC(
                        id = dto.id,
                        bpm = dto.bpm,
                        estado = dto.estado,
                        dispositivo = dto.dispositivo,
                        hora = dto.hora,
                        sincronizado = true
                    )
                }
                _historial.value = lecturas
                
                // Actualizar FC con el último registro del reloj
                val ultimaWear = lecturas.firstOrNull { it.dispositivo == "wear" }
                if (ultimaWear != null) {
                    SmartHealthRepository.actualizarFC(ultimaWear.bpm)
                }
            } catch (e: Exception) {
                android.util.Log.e("TV_VM", "Error al cargar datos de Neon: ${e.message}")
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

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
    }
}
