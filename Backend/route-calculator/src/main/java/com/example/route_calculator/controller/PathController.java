package com.example.route_calculator.controller;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.example.route_calculator.model.*;
import com.example.route_calculator.service.*;
import com.example.route_calculator.utils.AStar;
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
        @RequestParam(required = false) boolean includeIncidents) {
        Graph<Node, DefaultWeightedEdge> graph = GraphService.getGraph();
        AStar aStar = new AStar(graph);
        List<Node> path = aStar.findShortestPath(startLat, startLon, endLat, endLon, includeIncidents);
        if(path == null || path.isEmpty()) {
            return "{\"error\": \"No path found\"}";
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");

        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");
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
        featureCollection.set("features", mapper.createArrayNode().add(feature));

        return featureCollection.toString();
    }
}