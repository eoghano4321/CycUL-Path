package com.example.route_calculator.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Graph {
    private final Map<String, Node> nodes = new HashMap<>();
    public Node findClosestNode(double lat, double lng) { 
        // Implement nearest neighbor search for map data
        return new Node("1", lat, lng); 
    }

    public void addNode(String id, double lat, double lon) {
        nodes.putIfAbsent(id, new Node(id, lat, lon));
    }

    public void addEdge(String id1, String id2) {
        Node n1 = nodes.get(id1);
        Node n2 = nodes.get(id2);
        if (n1 != null && n2 != null) {
            n1.addNeighbor(n2);
            n2.addNeighbor(n1); // Undirected graph
        }
    }

    @JsonProperty("nodes")
    public Collection<Node> getNodes() {
        return nodes.values();
    }
}