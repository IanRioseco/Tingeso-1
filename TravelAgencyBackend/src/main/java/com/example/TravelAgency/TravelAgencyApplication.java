package com.example.TravelAgency;

import com.example.TravelAgency.config.TravelProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TravelProperties.class)
public class TravelAgencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelAgencyApplication.class, args);
	}

}
