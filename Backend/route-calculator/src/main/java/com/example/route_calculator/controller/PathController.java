package com.example.route_calculator.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.example.route_calculator.model.*;
import com.example.route_calculator.service.*;

@RestController
@RequestMapping("/api")
public class PathController {

    @GetMapping("/shortest-path")
    public PathResponse findShortestPath(@RequestParam double startLat, @RequestParam double startLng,
                                          @RequestParam double endLat, @RequestParam double endLng) {
        Graph graph = MapProcessor.loadGraph(); // Load preprocessed map data
        Node start = graph.findClosestNode(startLat, startLng);
        Node end = graph.findClosestNode(endLat, endLng);
        
        List<Node> path = AStar.findPath(graph, start, end);
        return new PathResponse(path);
    }
}