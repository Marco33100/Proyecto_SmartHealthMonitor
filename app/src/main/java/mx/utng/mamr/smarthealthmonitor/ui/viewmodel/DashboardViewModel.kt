package mx.utng.mamr.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.mamr.smarthealthmonitor.data.models.MockData

class DashboardViewModel : ViewModel() {

    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) MockData.fcActual else it }
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), MockData.fcActual)

    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .map { if (it == 0) MockData.pasosActual else it }
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), MockData.pasosActual)

    // ← NUEVO: historial desde Room (Flow reactivo)
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}

