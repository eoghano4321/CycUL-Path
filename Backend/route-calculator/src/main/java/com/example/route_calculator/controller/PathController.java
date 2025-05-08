package com.example.route_calculator.controller;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.example.route_calculator.model.*;
import com.example.route_calculator.service.*;
import com.example.route_calculator.utils.AStar;
import com.example.route_calculator.utils.DistanceCalculator;
import com.example.route_calculator.utils.WeightCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api")
public class PathController {
    @GetMapping("/shortest-path")
    public String getPathAsGeoJson(@RequestParam double startLat, 
        @RequestParam double startLon, 
        @RequestParam double endLat, 
        @RequestParam double endLon,
        @RequestParam(required = false, defaultValue = "false") boolean includeIncidents) {
        Graph<Node, DefaultWeightedEdge> graph = GraphService.getGraph();
        AStar aStar = new AStar(graph);
        
        List<Node> path;
        try {
            path = aStar.findShortestPath(startLat, startLon, endLat, endLon, includeIncidents);
        } catch (IllegalArgumentException e) {
             // Handle cases where start/end nodes are not found or no path exists
             ObjectMapper mapper = new ObjectMapper();
             ObjectNode errorResponse = mapper.createObjectNode();
             errorResponse.put("error", e.getMessage());
             return errorResponse.toString();
        }

        if(path == null || path.isEmpty()) {
             ObjectMapper mapper = new ObjectMapper();
             ObjectNode errorResponse = mapper.createObjectNode();
             errorResponse.put("error", "No path found");
             return errorResponse.toString();
        }

        // Calculate risk score using WeightCalculator for the entire path
        double totalIncidentWeight = 0.0;
        double totalDistance = 0.0;
        if (path.size() > 1) {
            for (int i = 0; i < path.size() - 1; i++) {
                Node node1 = path.get(i);
                Node node2 = path.get(i + 1);
                // Calculate the distance between the two nodes
                totalDistance += DistanceCalculator.haversine(node1.lat, node1.lon, node2.lat, node2.lon) * 1000;
                // Get the incident weight and convert to a percentage
                totalIncidentWeight += WeightCalculator.getIncidentWeight(node1, node2) * 150;
            }
        }
        
        double riskScore;
        if (totalDistance == 0.0) {
            riskScore = 0.0;
        } else {
            // Dividing by Math.sqrt(totalDistance) instead of totalDistance makes the score
            // less sensitive to the overall length of the route. This helps prevent
            // long routes from appearing to have an artificially low risk score simply
            // due to their length. The multiplier 10.0 is used to rescale the score
            // to a range comparable to the previous calculation, ensuring that a
            // 100m route with a given incident density retains a similar score,
            // while longer routes with similar incident densities will now score higher.
            double rawRiskScore = (totalIncidentWeight / Math.sqrt(totalDistance)) * 10.0;
            riskScore = Math.min(Math.round(rawRiskScore), 99.0);
        }
        
        double travelTime = (totalDistance / 3) / 60; // Assuming an average speed of 3 m/s

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");

        ArrayNode features = mapper.createArrayNode();
        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");

        // Add properties to the feature, including the calculated risk score
        ObjectNode properties = mapper.createObjectNode();
        properties.put("riskScore", riskScore); // Use the calculated total incident weight
        properties.put("travelTime", travelTime); // Add travel time to properties
        properties.put("totalDistance", totalDistance); // Add total distance to properties
        feature.set("properties", properties);

        ObjectNode geometry = mapper.createObjectNode();
        geometry.put("type", "LineString");
        ArrayNode coordinates = mapper.createArrayNode();

        for (Node node : path) {
            ArrayNode coord = mapper.createArrayNode();
            coord.add(node.lon);
            coord.add(node.lat);
            coordinates.add(coord);
        }

        geometry.set("coordinates", coordinates);
        feature.set("geometry", geometry);
        features.add(feature); // Add the feature to the features array
        featureCollection.set("features", features); // Set the features array in the collection

        return featureCollection.toString();
    }
}