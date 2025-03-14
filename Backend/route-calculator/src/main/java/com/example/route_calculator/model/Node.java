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

    // @JsonProperty("weight")
    // public double weight;

    @JsonIgnore
    public List<Node> neighbors = new ArrayList<>();

    public Node(){
    }

    public Node(String id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        // this.weight = weight;
    }

    @JsonProperty("neighbors")
    public List<String> getNeighborIds() {
        List<String> neighborIds = new ArrayList<>();
        for (Node neighbor : neighbors) {
            neighborIds.add(neighbor.id);
        }
        return neighborIds;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return Double.compare(node.lat, lat) == 0 && Double.compare(node.lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    @Override
    public String toString() {
        return "(" + lat + ", " + lon + ")";
    }

    void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }
}