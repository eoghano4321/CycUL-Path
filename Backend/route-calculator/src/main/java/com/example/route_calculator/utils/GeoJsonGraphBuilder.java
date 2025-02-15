package com.example.route_calculator.utils;

import java.util.ArrayList;
import java.util.List;

import com.example.route_calculator.model.*;
import com.fasterxml.jackson.databind.JsonNode;

public class GeoJsonGraphBuilder {
    public static Graph buildGraph(JsonNode geoJson) {
        Graph graph = new Graph();

        for (JsonNode feature : geoJson.get("features")) {
            String type = feature.get("geometry").get("type").asText();
            JsonNode coordinates = feature.get("geometry").get("coordinates");

            if ("Point".equals(type)) {
                double lon = coordinates.get(0).asDouble();
                double lat = coordinates.get(1).asDouble();
                String id = feature.get("properties").get("id").asText();
                graph.addNode(id, lat, lon);
            } else if ("LineString".equals(type)) {
                List<String> previousNodeIds = new ArrayList<>();
                for (JsonNode coord : coordinates) {
                    double lon = coord.get(0).asDouble();
                    double lat = coord.get(1).asDouble();
                    String id = lat + "," + lon; // Unique ID based on coordinates
                    graph.addNode(id, lat, lon);
                    previousNodeIds.add(id);
                }
                for (int i = 0; i < previousNodeIds.size() - 1; i++) {
                    graph.addEdge(previousNodeIds.get(i), previousNodeIds.get(i + 1));
                }
            }
        }
        return graph;
    }
}
