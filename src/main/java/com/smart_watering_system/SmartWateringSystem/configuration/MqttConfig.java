package com.smart_watering_system.SmartWateringSystem.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.service.MqttSevice;
import com.smart_watering_system.SmartWateringSystem.service.RealtimeService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
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
    RealtimeService realtimeService;

    @Bean
    MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(broker, clientId + UUID.randomUUID(), new MemoryPersistence());

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setConnectionTimeout(10);
        connectOptions.setKeepAliveInterval(30);

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                log.info("✅ MQTT connected to: {}", serverURI);
                try {
//                    mqttSevice.subcribeAsync("status/#",
//                            (topic, message) -> realtimeService.sendDeviceStatus(topic, message.toString()));
                    client.subscribe("status/#", (topic, message) ->
                            realtimeService.sendDeviceStatus(topic, message.toString()));
                } catch (MqttException e) {
                    log.error("subscribe status/# failed: {}", e.getMessage());
                    throw new RuntimeException(e);
                }

                try {
//                    mqttSevice.subcribeAsync("sensor/#",
//                            (topic, message) -> realtimeService.sendData(topic, message.toString()));
                    client.subscribe("sensor/#", (topic, message) ->
                            realtimeService.sendData(topic, message.toString()));
                } catch (MqttException e) {
                    log.error("subscribe sensor/# failed: {}", e.getMessage());
                    throw new RuntimeException(e);
                }

                try {
//                    mqttSevice.subcribeAsync("watering/#",
//                            (topic, message) -> realtimeService.sendPumpStatus(topic, message.toString()));
                    client.subscribe("watering/status/#", (topic, message) ->
                            realtimeService.sendPumpStatus(topic, message.toString()));
                } catch (MqttException e) {
                    log.error("subscribe watering/# failed: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                log.info("⚠️ MQTT connection lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage payload) {}

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        client.connect(connectOptions);

        return client;
    }

}
