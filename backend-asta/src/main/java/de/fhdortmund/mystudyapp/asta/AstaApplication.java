package de.fhdortmund.mystudyapp.asta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.integration.annotation.IntegrationComponentScan;

// Exclude the database auto-configurations so Spring Boot doesn't look for a DB
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@IntegrationComponentScan
public class AstaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstaApplication.class, args);
    }
}