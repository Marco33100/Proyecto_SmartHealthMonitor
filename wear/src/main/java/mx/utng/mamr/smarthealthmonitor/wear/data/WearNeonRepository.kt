package mx.utng.mamr.smarthealthmonitor.wear.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.mamr.smarthealthmonitor.data.remote.LecturaFcDto
import mx.utng.mamr.smarthealthmonitor.data.remote.NeonClient
import mx.utng.mamr.smarthealthmonitor.data.remote.NeonRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WearNeonRepository {
    /** El reloj solo PUBLICA lecturas — sin Room local */
    suspend fun publicarLectura(bpm: Int, estado: String) =
        withContext(Dispatchers.IO) {
            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            NeonClient.api.executeQuery(
                auth = NeonClient.AUTH_HEADER,
                connStr = NeonClient.CONN_STRING,
                request = NeonRequest(
                    query = "INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora) VALUES ($1, $2, $3, $4)",
                    params = listOf(bpm, estado, "wear", hora)
                )
            )
            android.util.Log.d("WEAR_DB", "⌚ FC enviada a Neon: ${bpm} bpm")
        }

    /** Obtener las últimas 5 lecturas del reloj desde Neon */
    suspend fun obtenerUltimasLecturas(): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            NeonClient.api.executeQuery(
                auth = NeonClient.AUTH_HEADER,
                connStr = NeonClient.CONN_STRING,
                request = NeonRequest(
                    query = "SELECT * FROM lecturas_fc WHERE dispositivo='wear' ORDER BY created_at DESC LIMIT 5"
                )
            ).rows
        }
}
