package com.example.route_calculator.utils;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.LoggerFactory;

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

import org.slf4j.Logger;

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

    private static final Logger logger = LoggerFactory.getLogger(AStar.class);
    
    private final Graph<Node, DefaultWeightedEdge> graph;

    public AStar(Graph<Node, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }

    public List<Node> findShortestPath(double startLat, double startLon, double endLat, double endLon) {
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
        openSet.add(new PathNode(startNode, 0.0, manhattanDistance(startNode, endNode)));

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (!visited.add(current.node)) continue;

            if (current.node.equals(endNode)) {
                return reconstructPath(cameFrom, current.node);
            }

            for (DefaultWeightedEdge edge : graph.edgesOf(current.node)) {
                Node neighbor = getNeighbor(current.node, edge);
                if (neighbor == null) continue;

                double edgeWeight = graph.getEdgeWeight(edge);
                double incidentWeight = getIncidentWeight(current.node, neighbor);
                double totalWeight = edgeWeight + incidentWeight;

                double tentativeGScore = gScores.getOrDefault(current.node, Double.MAX_VALUE) + totalWeight;

                if (tentativeGScore < gScores.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.node);
                    gScores.put(neighbor, tentativeGScore);
                    double fScore = tentativeGScore + GeoJsonGraphBuilder.haversine(neighbor.lat, neighbor.lon, endNode.lat, endNode.lon);
                    openSet.add(new PathNode(neighbor, tentativeGScore, fScore));
                }
            }
        }

        throw new IllegalArgumentException("No path found between the start and end nodes.");
    }

    private Node findClosestNode(double lat, double lon) {
        Node closestNode = graph.vertexSet().stream()
            .min((n1, n2) -> Double.compare(
                GeoJsonGraphBuilder.haversine(n1.lat, n1.lon, lat, lon),
                GeoJsonGraphBuilder.haversine(n2.lat, n2.lon, lat, lon)))
            .orElse(null);

        if (closestNode != null) {
            double distance = GeoJsonGraphBuilder.haversine(closestNode.lat, closestNode.lon, lat, lon);
            if (distance < 0.15) { // 15 meters in kilometers
                return closestNode;
            }
        }

        return null;
    }

    private double manhattanDistance(Node n1, Node n2) {
        return Math.abs(n1.lat - n2.lat) + Math.abs(n1.lon - n2.lon);
    }

    private double getIncidentWeight(Node n1, Node n2) {
        double minLat = Math.min(n1.lat, n2.lat);
        double maxLat = Math.max(n1.lat, n2.lat);
        double minLon = Math.min(n1.lon, n2.lon);
        double maxLon = Math.max(n1.lon, n2.lon);
        logger.debug("Incident weight for segment: {} to {}: minLat={}, maxLat={}, minLon={}, maxLon={}", n1, n2, minLat, maxLat, minLon, maxLon);
        return IncidentIndex.getIncidentWeight(minLat, maxLat, minLon, maxLon);
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
