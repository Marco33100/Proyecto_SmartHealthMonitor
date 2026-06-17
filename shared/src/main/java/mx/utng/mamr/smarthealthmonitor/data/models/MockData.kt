package mx.utng.mamr.smarthealthmonitor.data.models

import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC

// Datos de prueba para desarrollo (mock data)
object MockData {
    val historialFC = listOf(
        LecturaFC(1, 78),
        LecturaFC(2, 82),
        LecturaFC(3, 76),
        LecturaFC(4, 95),
        LecturaFC(5, 71),
        LecturaFC(6, 80),
        LecturaFC(7, 74)
    )
    var fcActual = 78
    var pasosActual = 4250
}
