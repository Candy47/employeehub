package com.employeehub.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class AuditServiceApplication {

	public static void main(String[] args) {
		// PostgreSQL 17 rejects the legacy JVM zone id "Asia/Calcutta"; pin a
		// zone id the server accepts before the JDBC driver connects.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(AuditServiceApplication.class, args);
	}
}

