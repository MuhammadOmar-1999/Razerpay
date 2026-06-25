package com.omar.razerpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RazerpayApplication {

	public static void main(String[] args) {
		SpringApplication.run(RazerpayApplication.class, args);
	}

}
