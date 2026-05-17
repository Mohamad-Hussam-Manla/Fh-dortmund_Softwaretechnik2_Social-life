package de.fhdortmund.backend_main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EntityScan(basePackages = "de.fhdortmund.mystudyapp")
@EnableJpaRepositories(basePackages = "de.fhdortmund.mystudyapp")
public class BackendMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendMainApplication.class, args);
	}

}