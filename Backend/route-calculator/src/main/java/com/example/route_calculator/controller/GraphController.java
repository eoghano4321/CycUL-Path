package com.example.route_calculator.controller;


import com.example.route_calculator.service.GraphService;
import com.example.route_calculator.model.Graph;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GraphController {
    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph")
    public Graph getGraph() {
        return graphService.getGraph();
    }
}
