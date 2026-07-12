package mx.utng.mamr.smarthealthmonitor.mqtt

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mx.utng.mamr.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.mamr.smarthealthmonitor.mqtt.MqttConfig
import mx.utng.mamr.smarthealthmonitor.mqtt.FcMessage
import mx.utng.mamr.smarthealthmonitor.mqtt.TvMessage
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MqttAppService(
    private val context : Context
) {
    private var client: MqttAsyncClient? = null

    fun connect() {
        try {
            client = MqttAsyncClient(
                MqttConfig.BROKER_URL,
                MqttConfig.CLIENT_APP, 
                MemoryPersistence()
            )
            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isCleanSession = true
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
            }
            
            // Callback de mensajes entrantes
            client?.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String, msg: MqttMessage) {
                    when (topic) {
                        MqttConfig.TOPIC_FC -> handleFcMessage(msg)
                    }
                }
                override fun connectionLost(cause: Throwable?) {
                    android.util.Log.w("MQTT_APP","Conexión perdida: ${cause?.message}")
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    // Suscribirse al topic de FC del reloj
                    client?.subscribe(MqttConfig.TOPIC_FC, MqttConfig.QOS)
                    android.util.Log.d("MQTT_APP","✅ Conectado y suscrito a ${MqttConfig.TOPIC_FC}")
                }
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    android.util.Log.e("MQTT_APP","❌ Error: ${ex?.message}")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "❌ Error al iniciar MqttAppService: ${e.message}")
        }
    }

    private fun handleFcMessage(msg: MqttMessage) {
        try {
            val payloadString = String(msg.payload)
            val fcMsg = Json.decodeFromString<FcMessage>(payloadString)
            
            // 1. Actualizar el Repository (actualiza el flow y persiste en Room)
            SmartHealthRepository.actualizarFC(fcMsg.bpm)
            
            // 2. Re-publicar al topic TV con formato enriquecido
            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val tvMsg = TvMessage(bpm = fcMsg.bpm, estado = fcMsg.estado, hora = hora)
            val tvPayload = Json.encodeToString(tvMsg).toByteArray()
            
            val tvMqtt = MqttMessage(tvPayload).apply {
                qos = MqttConfig.QOS
                isRetained = true
            }
            client?.publish(MqttConfig.TOPIC_TV, tvMqtt)
            android.util.Log.d("MQTT_APP","   Re-publicado al TV: ${fcMsg.bpm} bpm")
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "❌ Error al procesar mensaje FC: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (client?.isConnected == true) {
                client?.disconnect()
            }
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "❌ Error al desconectar MqttAppService: ${e.message}")
        }
    }
}
