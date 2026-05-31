package mx.utng.mamr.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.mamr.smarthealthmonitor.data.models.MockData

class DashboardViewModel : ViewModel() {

    // FC: viene del wearable real vía Repository.
    // Si es 0 (sin dato aún), usar valor simulado.
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) MockData.fcActual else it }
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = MockData.fcActual
        )

    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .map { if (it == 0) MockData.pasosActual else it }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.pasosActual
        )
    val historial = MockData.historialFC  // TODO S7: Room
}
