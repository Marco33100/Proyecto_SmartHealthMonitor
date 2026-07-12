package mx.utng.mamr.smarthealthmonitor.mqtt

import mx.utng.mamr.smarthealthmonitor.shared.BuildConfig

object MqttConfig {
    // Datos leídos de forma segura desde local.properties mediante BuildConfig
    val BROKER_URL = BuildConfig.MQTT_BROKER_URL.ifEmpty { "ssl://7c07f793f35e454184a3d3991019f8e1.s1.eu.hivemq.cloud:8883" }
    val USERNAME   = BuildConfig.MQTT_USERNAME.ifEmpty { "mamr-smarthealth" }
    val PASSWORD   = BuildConfig.MQTT_PASSWORD.ifEmpty { "mamr12345" }

    // Topics del proyecto
    const val TOPIC_FC    = "utng/smarthealthmonitor/fc"
    const val TOPIC_TV    = "utng/smarthealthmonitor/tv"
    const val TOPIC_ALERT = "utng/smarthealthmonitor/alerta"

    // QoS: 0 = best effort, 1 = at least once, 2 = exactly once
    const val QOS = 1

    // Client IDs únicos por dispositivo
    const val CLIENT_WEAR = "smarthealthmonitor-wear"
    const val CLIENT_APP  = "smarthealthmonitor-app"
    const val CLIENT_TV   = "smarthealthmonitor-tv"
}
