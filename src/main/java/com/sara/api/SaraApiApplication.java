package com.sara.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class SaraApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaraApiApplication.class, args);
	}

}
