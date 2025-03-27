package com.example.route_calculator.controller;


import com.example.route_calculator.model.GraphResponse;
import com.example.route_calculator.model.Node;
import com.example.route_calculator.service.GraphService;
import org.springframework.web.bind.annotation.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

@RestController
@RequestMapping("/api")
public class GraphController {
    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph")
    public GraphResponse getGraph() {
        return graphService.getGraphAsJson();
    }
}
