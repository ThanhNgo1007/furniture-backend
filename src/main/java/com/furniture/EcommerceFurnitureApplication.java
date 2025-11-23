package com.furniture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EcommerceFurnitureApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceFurnitureApplication.class, args);
	}

}
