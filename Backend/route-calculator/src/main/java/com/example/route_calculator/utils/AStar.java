package com.example.route_calculator.utils;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.example.route_calculator.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class AStar {
    private static class PathNode {
        Node node;
        double gScore;
        double fScore;

        PathNode(Node node, double gScore, double fScore) {
            this.node = node;
            this.gScore = gScore;
            this.fScore = fScore;
        }
    }

    private final Graph<Node, DefaultWeightedEdge> graph;

    public AStar(Graph<Node, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }

    public List<Node> findShortestPath(double startLat, double startLon, double endLat, double endLon, boolean includeIncidents) {
        Node startNode = findClosestNode(startLat, startLon);
        Node endNode = findClosestNode(endLat, endLon);

        if (startNode == null || endNode == null) {
            throw new IllegalArgumentException("Start or end node not found in the graph.");
        }

        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Node, Double> gScores = new HashMap<>();
        Map<Node, Node> cameFrom = new HashMap<>();

        Set<Node> visited = new HashSet<>();

        gScores.put(startNode, 0.0);
        openSet.add(new PathNode(startNode, 0.0, DistanceCalculator.haversine(startNode.lat, startNode.lon, endNode.lat, endNode.lon)));

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (!visited.add(current.node)) continue;

            if (current.node.equals(endNode)) {
                List<Node> path = reconstructPath(cameFrom, current.node);
                return path;
            }

            for (DefaultWeightedEdge edge : graph.edgesOf(current.node)) {
                Node neighbor = getNeighbor(current.node, edge);
                if (neighbor == null) continue;

                double edgeWeight = graph.getEdgeWeight(edge);
                double incidentWeight;
                if (includeIncidents) {
                    incidentWeight = WeightCalculator.getIncidentWeight(current.node, neighbor);
                } else {
                    incidentWeight = 0.0;
                }
                double turnPenalty = WeightCalculator.getTurnPenalty(cameFrom.get(current.node), current.node, neighbor);

                double totalWeight = edgeWeight + incidentWeight + turnPenalty;

                double tentativeGScore = gScores.getOrDefault(current.node, Double.MAX_VALUE) + totalWeight;

                if (tentativeGScore < gScores.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.node);
                    gScores.put(neighbor, tentativeGScore);

                    double hScore = DistanceCalculator.haversine(neighbor.lat, neighbor.lon, endNode.lat, endNode.lon);
                    double fScore = tentativeGScore + hScore + (1e-6 * hScore);

                    openSet.add(new PathNode(neighbor, tentativeGScore, fScore));
                }
            }
        }

        throw new IllegalArgumentException("No path found between the start and end nodes.");
    }    

    private Node findClosestNode(double lat, double lon) {
        Node closestNode = graph.vertexSet().stream()
            .min((n1, n2) -> Double.compare(
                DistanceCalculator.haversine(n1.lat, n1.lon, lat, lon),
                DistanceCalculator.haversine(n2.lat, n2.lon, lat, lon)))
            .orElse(null);

        if (closestNode != null) {
            double distance = DistanceCalculator.haversine(closestNode.lat, closestNode.lon, lat, lon);
            if (distance < 0.15) { // 15 meters in kilometers
                return closestNode;
            }
        }

        return null;
    }

    private Node getNeighbor(Node node, DefaultWeightedEdge edge) {
        return graph.getEdgeSource(edge).equals(node) ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge);
    }

    private List<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        List<Node> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }
}
