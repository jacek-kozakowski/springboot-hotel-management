package com.reservations.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
@EnableScheduling
public class HotelApplication {
	public static void main(String[] args) {
		SpringApplication.run(HotelApplication.class, args);
	}

}
