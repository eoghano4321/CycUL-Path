package com.example.route_calculator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader; // Added import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value; // Added import

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode; // Added import

@RestController
@RequestMapping("/api/display")
public class UIController {

    @Value("${API_AUTH_TOKEN}") // Injects the value of the API_AUTH_TOKEN environment variable
    private String expectedAuthToken;

    private JsonNode handleUnauthorized() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorResponse = mapper.createObjectNode();
        System.err.println("Unauthorized access attempt detected.");
        errorResponse.put("error", "Unauthorized: Missing or invalid token");
        return errorResponse;
    }

    @GetMapping("/map")
    public JsonNode getMap(@RequestHeader("Authorization") String receivedToken) { // Added Authorization header
        // Check if the received token matches the expected token from the environment variable
        if (expectedAuthToken == null || expectedAuthToken.isEmpty() || !expectedAuthToken.equals(receivedToken)) {
            return handleUnauthorized();
        }
        try {
            System.out.println("Received valid request for map data.");
            byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/OSM_Dublin_CycleNetwork.geojson"));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/incidents")
    public JsonNode getIncidents(@RequestHeader("Authorization") String receivedToken){ // Added Authorization header
        // Check if the received token matches the expected token from the environment variable
        if (expectedAuthToken == null || expectedAuthToken.isEmpty() || !expectedAuthToken.equals(receivedToken)) {
            return handleUnauthorized();
        }
        try {
            System.out.println("Received valid request for incident data.");
            byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/dublin_incidents_mar2025.geojson"));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonData);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
