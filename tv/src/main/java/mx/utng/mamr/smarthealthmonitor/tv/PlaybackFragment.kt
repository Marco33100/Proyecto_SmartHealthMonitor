package mx.utng.mamr.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.PlaybackSupportFragment
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.utng.mamr.smarthealthmonitor.data.db.SmartHealthDB

class PlaybackFragment : PlaybackSupportFragment() {
    private lateinit var player: ExoPlayer
    private var totalSecondsPlayed = 0

    companion object {
        private const val UPDATE_DELAY_MS = 16
        const val ARG_URL = "media_url"
        const val ARG_TITLE = "media_title"
        const val ARG_LECTURA_ID = "lectura_id"

        fun newInstance(url: String, title: String, lecturaId: Int): PlaybackFragment =
            PlaybackFragment().apply {
                arguments = Bundle().also {
                    it.putString(ARG_URL, url)
                    it.putString(ARG_TITLE, title)
                    it.putInt(ARG_LECTURA_ID, lecturaId)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(ARG_URL) ?: return
        val title = arguments?.getString(ARG_TITLE) ?: ""

        // 1. Crear el motor de reproducción
        player = ExoPlayer.Builder(requireContext()).build()

        // 2. Conectar con la UI de Leanback
        val adapter = LeanbackPlayerAdapter(
            requireContext(), player, UPDATE_DELAY_MS
        )
        val glue = PlaybackTransportControlGlue(requireContext(), adapter).apply {
            this.title = title
            this.subtitle = "SmartHealth Monitor"
            host = PlaybackSupportFragmentGlueHost(this@PlaybackFragment)
            playWhenPrepared()
        }

        // 3. Cargar y reproducir el media
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()

        // 4. Reto adicional: ProgressListener para registrar en Room cuánto se reprodujo
        player.addListener(object : Player.Listener {
            private var startTime = 0L

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startTime = System.currentTimeMillis()
                } else {
                    if (startTime != 0L) {
                        val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                        if (elapsed > 0) {
                            registrarDuracion(elapsed)
                        }
                        startTime = 0L
                    }
                }
            }
        })
    }

    private fun registrarDuracion(segundos: Int) {
        totalSecondsPlayed += segundos
        val id = arguments?.getInt(ARG_LECTURA_ID) ?: return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = SmartHealthDB.getDatabase(requireContext())
            db.lecturaDao().actualizarDuracion(id, totalSecondsPlayed)
        }
    }

    // ⚠️ SIEMPRE liberar ExoPlayer — error crítico olvidarlo
    override fun onDestroyView() {
        super.onDestroyView()
        if (::player.isInitialized) {
            player.release()
        }
    }
}
