package com.example.route_calculator.model;

import java.util.List;

public class GraphResponse {
    private List<Node> nodes;
    private List<EdgeResponse> edges;

    public GraphResponse() {
    }

    public GraphResponse(List<Node> nodes, List<EdgeResponse> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<EdgeResponse> getEdges() {
        return edges;
    }

    public static class EdgeResponse {
        private Node source;
        private Node target;
        private double weight;

        public EdgeResponse(){};

        public EdgeResponse(Node source, Node target, double weight) {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }

        public Node getSource() {
            return source;
        }

        public Node getTarget() {
            return target;
        }

        public double getWeight() {
            return weight;
        }
    }
}
