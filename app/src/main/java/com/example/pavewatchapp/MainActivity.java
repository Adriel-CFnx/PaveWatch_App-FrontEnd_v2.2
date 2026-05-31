package com.example.pavewatchapp; // <-- ¡DEJA LA PRIMERA LÍNEA QUE TENÍAS TÚ!

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Estas son las herramientas que te faltaban (ya no saldrá en rojo)
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnReportar = findViewById(R.id.btnReportar);

        // Al presionar el botón...
        btnReportar.setOnClickListener(v -> {
            Toast.makeText(this, "Enviando bache a HiveMQ...", Toast.LENGTH_SHORT).show();
            enviarBacheMqtt();
        });
    }

    private void enviarBacheMqtt() {
        // En Android, el internet DEBE ir en un hilo secundario
        new Thread(() -> {
            // TU URL DE HIVEMQ (Asegúrate de que diga ssl:// y termine en 8883)
            String brokerUrl = "ssl://118c4eb634124e6a914d87e18ad2ea02.s1.eu.hivemq.cloud:8883";
            String clientId = MqttClient.generateClientId();

            try {
                MqttClient mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();

                // TUS CREDENCIALES EXACTAS (Las de Access Management)
                options.setUserName("admin");
                options.setPassword("Admin123".toCharArray());

                options.setCleanSession(true);

                // Nos conectamos a la nube
                mqttClient.connect(options);

                // Armamos el JSON del bache simulado
                String payload = "{\"latitud\": -12.0163, \"longitud\": -77.0495, \"severidad\": 9.5, \"dispositivo\": \"app_nativa_sebas\"}";

                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);

                // ¡Fuego! Mandamos el mensaje al topic
                mqttClient.publish("pavewatch/alertas", message);

                // Nos desconectamos
                mqttClient.disconnect();

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }).start();
    }
}