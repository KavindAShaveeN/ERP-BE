package com.rr.erp;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ErpApplication {

	public static void main(String[] args) {
		// Force IST (+05:30) for the JVM, JDBC session and all LocalDateTime.now() calls
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(ErpApplication.class, args);
	}

}
