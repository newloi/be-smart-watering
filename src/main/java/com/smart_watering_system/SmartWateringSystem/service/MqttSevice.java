package com.smart_watering_system.SmartWateringSystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MqttSevice {

    MqttClient mqttClient;

    @Async
    public void subcribeAsync(String topicSensor, String topicWatering) throws MqttException {
        mqttClient.subscribe(new String[]{topicSensor, topicWatering}, new int[]{1, 1});
    }

    @Async
    public void publishAsync(String topic, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(1);

        mqttClient.publish(topic, mqttMessage);
    }

}
