package com.smart_watering_system.SmartWateringSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableCaching
public class SmartWateringSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartWateringSystemApplication.class, args);
	}

}
