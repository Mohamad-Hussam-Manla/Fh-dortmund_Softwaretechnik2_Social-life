package com.social.event;

import com.social.event.mqqt.MqttSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventServiceApplication {

	public static void main(String[] args) {
		System.out.println("User Service läuft...");
		MqttSubscriber.subscribe();
		SpringApplication.run(EventServiceApplication.class, args);

	}

}
