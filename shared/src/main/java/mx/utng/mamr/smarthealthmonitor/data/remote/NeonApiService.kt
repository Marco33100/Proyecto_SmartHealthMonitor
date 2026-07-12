package mx.utng.mamr.smarthealthmonitor.data.remote

import retrofit2.http.*

/** Request genérico para la Neon HTTP API */
data class NeonRequest(val query: String, val params: List<Any> = emptyList())

/** Response de la Neon HTTP API */
data class NeonResponse<T>(
    val rows : List<T> = emptyList(),
    val rowCount : Int = 0,
    val command : String = "",
)

/** DTO de lectura FC (mapea fila de PostgreSQL) */
data class LecturaFcDto(
    val id : Int = 0,
    val bpm : Int,
    val estado : String,
    val dispositivo : String,
    val hora : String,
    val fecha : String = "",
    val created_at : String = "",
)

/** Interfaz Retrofit para la Neon HTTP API */
interface NeonApiService {
    @POST("sql")
    suspend fun executeQuery(
        @Header("Authorization") auth: String,
        @Header("Neon-Connection-String") connStr: String,
        @Body request: NeonRequest
    ): NeonResponse<LecturaFcDto>
}
