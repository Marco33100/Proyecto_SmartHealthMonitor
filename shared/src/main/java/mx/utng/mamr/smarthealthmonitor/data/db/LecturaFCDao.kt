package mx.utng.mamr.smarthealthmonitor.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaFCDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lectura: LecturaFC)

    // Flow: actualización reactiva cuando hay nuevos datos
    @Query("""
        SELECT * FROM lecturas_fc
        ORDER BY timestamp DESC
        LIMIT 50""")  // últimas 50 lecturas
    fun obtenerUltimas(): Flow<List<LecturaFC>>

    @Query("SELECT COUNT(*) FROM lecturas_fc")
    suspend fun contarRegistros(): Int

    // Limpiar lecturas más antiguas de 7 días
    @Query("""
        DELETE FROM lecturas_fc
        WHERE timestamp < :limite""")
    suspend fun limpiarViejos(limite: Long)
}
