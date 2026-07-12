package com.cfs.BMS2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Bms2Application {

	public static void main(String[] args) {
		SpringApplication.run(Bms2Application.class, args);
	}

}
