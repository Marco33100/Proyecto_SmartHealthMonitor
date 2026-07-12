package mx.utng.mamr.smarthealthmonitor.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.mamr.smarthealthmonitor.data.remote.LecturaFcDto
import mx.utng.mamr.smarthealthmonitor.data.remote.NeonClient
import mx.utng.mamr.smarthealthmonitor.data.remote.NeonRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TvNeonRepository {
    /** La TV PUBLICA lecturas cuando recibe datos vía MQTT */
    suspend fun publicarLectura(bpm: Int, estado: String) =
        withContext(Dispatchers.IO) {
            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            NeonClient.api.executeQuery(
                request = NeonRequest(
                    query = "INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora) VALUES ($1, $2, $3, $4)",
                    params = listOf(bpm, estado, "tv", hora)
                )
            )
            android.util.Log.d("TV_DB", "📺 FC enviada a Neon: $bpm bpm")
        }

    /** Obtener historial completo de los 3 dispositivos */
    suspend fun obtenerHistorialCompleto(limite: Int = 50): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            NeonClient.api.executeQuery(
                request = NeonRequest(
                    query = """SELECT id, bpm, estado, dispositivo, hora, created_at
                            FROM lecturas_fc
                            ORDER BY created_at DESC
                            LIMIT $1""".trimIndent(),
                    params = listOf(limite)
                )
            ).rows
        }

    /** Estadísticas por dispositivo */
    suspend fun obtenerEstadisticas(): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            NeonClient.api.executeQuery(
                request = NeonRequest(
                    query = """SELECT dispositivo,
                            ROUND(AVG(bpm)) AS bpm,
                            'Promedio' AS estado,
                            MAX(hora) AS hora
                            FROM lecturas_fc
                            GROUP BY dispositivo""".trimIndent()
                )
            ).rows
        }
}
