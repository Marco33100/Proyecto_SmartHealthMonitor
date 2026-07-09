package mx.utng.mamr.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.mamr.smarthealthmonitor.data.db.SmartHealthDB

class DetailFragment : DetailsSupportFragment(), OnActionClickedListener {
    
    private var lectura: LecturaFC? = null

    companion object {
        const val ARG_LECTURA_ID = "lectura_id"
        const val ACTION_PLAY = 1L
        const val ACTION_BACK = 2L
        const val ACTION_TREND = 3L

        fun newInstance(lecturaId: Int): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().also {
                    it.putInt(ARG_LECTURA_ID, lecturaId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getInt(ARG_LECTURA_ID) ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Buscar la lectura en Room por ID
            val db = SmartHealthDB.getDatabase(requireContext())
            lectura = db.lecturaDao().obtenerPorId(id)
            lectura?.let { construirDetalle(it) }
        }
    }

    private fun construirDetalle(lectura: LecturaFC) {
        val selector = ClassPresenterSelector()
        val dpPresenter = FullWidthDetailsOverviewRowPresenter(
            DetailsDescriptionPresenter()
        )
        dpPresenter.setOnActionClickedListener(this)
        selector.addClassPresenter(DetailsOverviewRow::class.java, dpPresenter)

        val row = DetailsOverviewRow(lectura)
        
        // Ícono de corazón como imagen del detalle
        val iconRes = if (lectura.esNormal)
            android.R.drawable.ic_menu_compass // placeholder OK
        else
            android.R.drawable.ic_dialog_alert // placeholder error
            
        row.imageDrawable = ContextCompat.getDrawable(requireContext(), iconRes)

        // Botones de acción
        val actions = ArrayObjectAdapter()
        actions.add(Action(ACTION_PLAY, "▶ Reproducir alerta"))
        actions.add(Action(ACTION_BACK, "← Volver al historial"))
        actions.add(Action(ACTION_TREND, "📈 Ver tendencia")) // Reto adicional
        row.actionsAdapter = actions

        val adapter = ArrayObjectAdapter(selector)
        adapter.add(row)
        this.adapter = adapter
    }

    override fun onActionClicked(action: Action) {
        when (action.id) {
            ACTION_PLAY -> {
                val playback = PlaybackFragment.newInstance(
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    title = "Alerta FC ${lectura?.valorBpm ?: 0} bpm",
                    lecturaId = lectura?.id ?: 0
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_browse_fragment, playback)
                    .addToBackStack(null)
                    .commit()
            }
            ACTION_BACK -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            ACTION_TREND -> {
                // Reto adicional: Mostrar Toast con los últimos 5 valores de FC ordenados cronológicamente
                viewLifecycleOwner.lifecycleScope.launch {
                    val db = SmartHealthDB.getDatabase(requireContext())
                    val flowList = db.lecturaDao().obtenerUltimas()
                    val totalList = flowList.first()
                    val ultimos5 = totalList.take(5).reversed()
                    val texto = ultimos5.joinToString(", ") { "${it.valorBpm} bpm" }
                    Toast.makeText(context, "Tendencia: $texto", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
