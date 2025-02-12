package com.example.route_calculator.model;

import java.util.Map;
import java.util.List;

public class Graph {
    private Map<Node, List<Edge>> adjacencyList;
    public Node findClosestNode(double lat, double lng) { 
        // Implement nearest neighbor search for map data
        return new Node(lat, lng); 
    }
}