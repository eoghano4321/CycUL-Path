package com.example.route_calculator.model;
import java.util.List;

public class PathResponse {
    private List<Node> path;

    public PathResponse(){
    }
    
    public PathResponse(List<Node> path) {
        this.path = path;
    }

    public List<Node> getPath() {
        return path;
    }
}
