package com.social.user;

import com.social.user.mqtt.MqttPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		System.out.println("Projekt: user-service");
		MqttPublisher.publish("Hello from User Service");
		SpringApplication.run(UserServiceApplication.class, args);

	}

}
