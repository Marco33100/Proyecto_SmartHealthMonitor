package mx.utng.mamr.smarthealthmonitor.tv.mqtt

import android.content.Context
import kotlinx.serialization.json.Json
import mx.utng.mamr.smarthealthmonitor.mqtt.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlinx.coroutines.flow.MutableStateFlow

class MqttTvSubscriber(
    private val context : Context,
    private val tvFlow : MutableStateFlow<TvMessage?> // actualiza el ViewModel
) {
    private var client: MqttAsyncClient? = null

    fun connect() {
        try {
            client = MqttAsyncClient(
                MqttConfig.BROKER_URL,
                MqttConfig.CLIENT_TV, 
                MemoryPersistence()
            )
            
            client?.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String, msg: MqttMessage) {
                    if (topic == MqttConfig.TOPIC_TV) {
                        try {
                            val tvMsg = Json.decodeFromString<TvMessage>(String(msg.payload))
                            tvFlow.value = tvMsg
                            android.util.Log.d("MQTT_TV", "   Recibido: ${tvMsg.bpm} bpm")
                        } catch (e: Exception) {
                            android.util.Log.e("MQTT_TV", "Error decodificando mensaje: ${e.message}")
                        }
                    }
                }
                override fun connectionLost(cause: Throwable?) {
                    android.util.Log.w("MQTT_TV", "Conexión de TV perdida: ${cause?.message}")
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isCleanSession = true
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
            }

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    client?.subscribe(MqttConfig.TOPIC_TV, MqttConfig.QOS)
                    android.util.Log.d("MQTT_TV", "✅ TV suscrita a ${MqttConfig.TOPIC_TV}")
                }
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    android.util.Log.e("MQTT_TV", "❌ Error detallado", ex)
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MQTT_TV", "❌ Error al iniciar MqttTvSubscriber: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (client?.isConnected == true) {
                client?.disconnect()
            }
        } catch (e: Exception) {
            android.util.Log.e("MQTT_TV", "❌ Error al desconectar: ${e.message}")
        }
    }
}
