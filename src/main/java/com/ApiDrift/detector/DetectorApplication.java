package com.ApiDrift.detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DetectorApplication.class, args);
	}

}
