package com.example.route_calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired; // Import Autowired
import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory

@SpringBootApplication
public class RouteCalculatorApplication {

	private static final Logger logger = LoggerFactory.getLogger(RouteCalculatorApplication.class); // Initialize logger

	@Autowired
	private LastRequestStore lastRequestStore; // Inject LastRequestStore

	public static void main(String[] args) {
		SpringApplication.run(RouteCalculatorApplication.class, args);
	}

	@PreDestroy
	public void onShutdown() {
		logger.info("Application is shutting down...");
		String lastRequest = lastRequestStore.getLastRequestDetails();
		if (lastRequest != null && !lastRequest.isEmpty()) {
			logger.info("Last request before shutdown:\n{}", lastRequest);
		} else {
			logger.info("No requests were processed or last request details are unavailable.");
		}
		// Add logic here to determine and log the specific reason for shutdown if possible
	}
}
