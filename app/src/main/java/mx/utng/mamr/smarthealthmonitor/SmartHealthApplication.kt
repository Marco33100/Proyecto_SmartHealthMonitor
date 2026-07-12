package mx.utng.mamr.smarthealthmonitor

import mx.utng.mamr.smarthealthmonitor.mqtt.MqttAppService

class SmartHealthApplication : SmartHealthApp() {
    
    lateinit var mqttService: MqttAppService

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar el servicio de MQTT del teléfono
        mqttService = MqttAppService(context = this)
        mqttService.connect()
    }
}
