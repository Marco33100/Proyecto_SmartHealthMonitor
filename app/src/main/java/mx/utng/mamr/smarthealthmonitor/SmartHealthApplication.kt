package mx.utng.mamr.smarthealthmonitor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mx.utng.mamr.smarthealthmonitor.mqtt.MqttAppService

class SmartHealthApplication : SmartHealthApp() {
    
    lateinit var mqttService: MqttAppService
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar el servicio de MQTT del teléfono
        mqttService = MqttAppService(context = this)
        applicationScope.launch {
            mqttService.connect()
        }

        // Programar sincronización periódica con Neon Serverless
        mx.utng.mamr.smarthealthmonitor.data.sync.NeonSyncWorker.schedule(this)
    }
}
