package com.social.user.mqtt;
import org.eclipse.paho.client.mqttv3.*;
public class MqttPublisher {
    public static void publish(String message) {
        try {
            String broker = "tcp://localhost:1883";
            String clientId = "user-service";

            MqttClient client = new MqttClient(broker, clientId);
            client.connect();

            MqttMessage msg = new MqttMessage(message.getBytes());
            client.publish("test/topic", msg);

            System.out.println("Sent: " + message);

            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
