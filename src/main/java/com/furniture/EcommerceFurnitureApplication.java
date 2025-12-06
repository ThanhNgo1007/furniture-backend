package com.furniture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling // Enable @Scheduled annotations
public class EcommerceFurnitureApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceFurnitureApplication.class, args);
	}

}
