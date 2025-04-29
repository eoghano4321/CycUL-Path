package com.example.route_calculator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/display")
public class UIController {
    @GetMapping("/map")
    public JsonNode getMap() {
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/OSM_Dublin_CycleNetwork.geojson"));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/incidents")
    public JsonNode getIncidents(){
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/dublin_incidents_mar2025.geojson"));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonData);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
