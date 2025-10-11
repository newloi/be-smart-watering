package com.smart_watering_system.SmartWateringSystem.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.service.SensorService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MqttConfig {

    @Value("${mqtt.broker}")
    String broker;

    @Value("${mqtt.clientId}")
    String clientId;

    @Value("${mqtt.username}")
    String username;

    @Value("${mqtt.password}")
    String password;

    @Autowired
    SensorService sensorService;

    @Bean
    MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(broker, clientId + UUID.randomUUID(), new MemoryPersistence());

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setConnectionTimeout(10);

        client.connect(connectOptions);
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("✅ MQTT connected to: " + serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("⚠️ MQTT connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage payload) throws JsonProcessingException {
                String message = new String(payload.getPayload(), StandardCharsets.UTF_8);
                sensorService.sendData(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        return client;
    }

}
