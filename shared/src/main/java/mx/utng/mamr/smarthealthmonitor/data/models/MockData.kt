package mx.utng.mamr.smarthealthmonitor.data.models

import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC

// Datos de prueba para desarrollo (mock data)
object MockData {
    val historialFC = listOf(
        LecturaFC(1, 78, "Normal", "app"),
        LecturaFC(2, 82, "Normal", "app"),
        LecturaFC(3, 76, "Normal", "app"),
        LecturaFC(4, 95, "Normal", "app"),
        LecturaFC(5, 71, "Normal", "app"),
        LecturaFC(6, 80, "Normal", "app"),
        LecturaFC(7, 74, "Normal", "app")
    )
    var fcActual = 78
    var pasosActual = 4250
}
