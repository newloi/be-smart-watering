package com.smart_watering_system.SmartWateringSystem.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MqttSevice {

    MqttClient mqttClient;

    @Async
    public void subcribeAsync(String toTopic, IMqttMessageListener callback) throws MqttException {
        mqttClient.subscribe(toTopic, callback);
    }

    @Async
    public void publishAsync(String topic, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(1);

        mqttClient.publish(topic, mqttMessage);
    }

    @Async
    public void unsubscribeAsync(String[] topic) throws MqttException {
        mqttClient.unsubscribe(topic);
    }

}
