package mx.utng.mamr.smarthealthmonitor.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFCDao
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.mamr.smarthealthmonitor.data.remote.NeonClient
import mx.utng.mamr.smarthealthmonitor.data.remote.NeonRequest

class SyncRepository(
    private val dao: LecturaFCDao // Room local
) {
    // ── LECTURA LOCAL (offline-first) ─────────────────────
    /** Flow reactivo de Room — siempre disponible sin internet */
    fun observarHistorial(): Flow<List<LecturaFC>> = dao.obtenerTodas()

    // ── ESCRITURA LOCAL + SYNC ──────────────────────────────
    /**
     * Guarda en Room primero (garantiza persistencia local),
     * luego intenta sincronizar con Neon en background.
     */
    suspend fun insertarLectura(lectura: LecturaFC) {
        // 1. Guardar localmente PRIMERO (nunca falla)
        val id = dao.insertar(lectura)
        
        // 2. Intentar sync con Neon (puede fallar sin internet)
        try {
            sincronizarHaciaNeon(lectura.copy(id = id.toInt()))
            dao.marcarSincronizado(id)
        } catch (e: Exception) {
            // Sin internet: quedará pendiente para el próximo sync
            android.util.Log.w("SYNC", "Pendiente de sync: ${e.message}")
        }
    }

    // ── PUSH: Room → Neon ──────────────────────────────────
    private suspend fun sincronizarHaciaNeon(lectura: LecturaFC) =
        withContext(Dispatchers.IO) {
            NeonClient.api.executeQuery(
                auth = NeonClient.AUTH_HEADER,
                connStr = NeonClient.CONN_STRING,
                request = NeonRequest(
                    query = """INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora)
                            VALUES ($1, $2, $3, $4) RETURNING id""".trimIndent(),
                    params = listOf(lectura.bpm, lectura.estado, lectura.dispositivo, lectura.hora)
                )
            )
        }

    // ── PULL: Neon → Room ──────────────────────────────────
    /**
     * Descarga los registros más recientes de Neon
     * y actualiza Room si hay datos nuevos.
     */
    suspend fun sincronizarDesdeNeon(limite: Int = 50) = withContext(Dispatchers.IO) {
        val response = NeonClient.api.executeQuery(
            auth = NeonClient.AUTH_HEADER,
            connStr = NeonClient.CONN_STRING,
            request = NeonRequest(
                query = "SELECT id,bpm,estado,dispositivo,hora FROM lecturas_fc ORDER BY created_at DESC LIMIT $1",
                params = listOf(limite)
            )
        )
        // Insertar en Room solo los que no existen (upsert)
        response.rows.forEach { dto ->
            dao.upsert(
                LecturaFC(
                    id = dto.id,
                    bpm = dto.bpm,
                    estado = dto.estado,
                    dispositivo = dto.dispositivo,
                    hora = dto.hora,
                    sincronizado = true
                )
            )
        }
        android.util.Log.d("SYNC", "✅ ${response.rowCount} registros descargados de Neon")
    }

    /** Sincronizar los pendientes que no llegaron al server */
    suspend fun enviarPendientes() = withContext(Dispatchers.IO) {
        val pendientes = dao.obtenerNoSincronizados()
        pendientes.forEach { lectura ->
            try {
                sincronizarHaciaNeon(lectura)
                dao.marcarSincronizado(lectura.id.toLong())
                android.util.Log.d("SYNC", "✅ Sincronizado pendiente id=${lectura.id}")
            } catch (e: Exception) {
                android.util.Log.w("SYNC", "Aún sin internet: ${e.message}")
            }
        }
    }
}
