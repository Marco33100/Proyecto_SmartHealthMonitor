package mx.utng.mamr.smarthealthmonitor

import android.app.Application
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository

class SmartHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa el repositorio singleton al arrancar la app
        SmartHealthRepository.init(this)
    }
}
