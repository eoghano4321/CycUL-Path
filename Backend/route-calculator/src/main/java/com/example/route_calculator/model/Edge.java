package com.example.route_calculator.model;

public class Edge {
    Node from, to;
    double cost;
    public Edge(Node from, Node to, double cost) { this.from = from; this.to = to; this.cost = cost; }
}