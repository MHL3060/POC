package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
public class SpringIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationApplication.class, args);
	}

}
