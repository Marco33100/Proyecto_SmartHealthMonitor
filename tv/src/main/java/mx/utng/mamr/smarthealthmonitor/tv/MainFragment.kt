package mx.utng.mamr.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC

class MainFragment : BrowseSupportFragment() {

    private val viewModel: TvViewModel by viewModels()
    private lateinit var estadoAdapter: ArrayObjectAdapter
    private lateinit var histAdapter: ArrayObjectAdapter
    private lateinit var alertaAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configuración del BrowseFragment
        title = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        
        // Color de la marca en el sidebar
        brandColor = resources.getColor(R.color.sh_primary, null)
        
        // Configuración de clics en tarjetas
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is EstadoActualTV -> {
                    // Simular nueva medición de pulso/pasos al hacer clic en Estado Actual
                    val nuevoBpm = (60..140).random()
                    val nuevosPasos = mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository.pasosFlow.value + (50..200).random()
                    mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository.actualizarFC(nuevoBpm)
                    mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository.actualizarPasos(nuevosPasos)
                    
                    android.widget.Toast.makeText(
                        context,
                        "Simulado en TV: $nuevoBpm bpm | Pasos: $nuevosPasos",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                is LecturaFC -> {
                    // Navegar a la pantalla de detalle al hacer clic en un registro del historial
                    val detail = DetailFragment.newInstance(item.id)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_browse_fragment, detail)
                        .addToBackStack(null) // Back regresa al BrowseFragment
                        .commit()
                }
            }
        }
        
        cargarFilas()
        observarDatos()
    }

    private fun observarDatos() {
        // Observar FC actual y Pasos actual para actualizar la Fila 1 (Estado actual)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.fc, viewModel.pasos) { fcVal, pasosVal ->
                    Pair(fcVal, pasosVal)
                }.collect { (fcVal, pasosVal) ->
                    estadoAdapter.clear()
                    
                    // Tarjeta de FC
                    val fcEsNormal = fcVal in 60..100
                    val fcColor = if (fcEsNormal) "#1B4F8A" else "#B3261E"
                    estadoAdapter.add(EstadoActualTV("$fcVal bpm", "Ahora", fcColor))
                    
                    // Tarjeta de Pasos (usando color sh_amber #D4860A)
                    estadoAdapter.add(EstadoActualTV("$pasosVal", "Pasos", "#D4860A"))
                }
            }
        }

        // Observar historial de Room y actualizar la Fila 2 (Historial FC)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historial.collect { lecturas ->
                    histAdapter.clear()
                    lecturas.forEach { histAdapter.add(it) }
                }
            }
        }
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        // Fila 1: Estado actual (FC + Pasos) reactivo
        estadoAdapter = ArrayObjectAdapter(FCCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Estado actual"), estadoAdapter))
        
        // Fila 2: Historial de FC reactivo
        histAdapter = ArrayObjectAdapter(FCCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Historial FC"), histAdapter))
        
        // Fila 3: Alertas recientes (Reto adicional)
        alertaAdapter = ArrayObjectAdapter(FCCardPresenter())
        val mockAlertas = listOf(
            AlertaTV("FC Elevada", "10:30", esCritica = true),
            AlertaTV("Batería Baja", "11:15", esCritica = false),
            AlertaTV("FC Elevada", "12:05", esCritica = true)
        )
        mockAlertas.forEach { alertaAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem("Alertas recientes"), alertaAdapter))
        
        this.adapter = rowsAdapter
    }
}
