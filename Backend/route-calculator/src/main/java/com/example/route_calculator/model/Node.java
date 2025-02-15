package com.example.route_calculator.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Node {
    @JsonProperty("id")
    public String id;

    @JsonProperty("lat")
    public double lat;

    @JsonProperty("lon")
    public double lon;

    @JsonIgnore
    public List<Node> neighbors = new ArrayList<>();

    public Node(String id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    @JsonProperty("neighbors")
    public List<String> getNeighborIds() {
        List<String> neighborIds = new ArrayList<>();
        for (Node neighbor : neighbors) {
            neighborIds.add(neighbor.id);
        }
        return neighborIds;
    }

    void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }
}