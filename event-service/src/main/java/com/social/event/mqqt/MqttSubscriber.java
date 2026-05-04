package com.social.event.mqqt;

import org.eclipse.paho.client.mqttv3.*;

public class MqttSubscriber {

    public static void subscribe() {
        try {
            String broker = "tcp://localhost:1883";
            String clientId = "event-service";

            MqttClient client = new MqttClient(broker, clientId);

            client.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {}

                public void messageArrived(String topic, MqttMessage message) {
                    System.out.println("Received: " + new String(message.getPayload()));
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            client.connect();
            client.subscribe("test/topic");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
