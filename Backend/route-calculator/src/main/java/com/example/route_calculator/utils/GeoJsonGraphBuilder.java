package com.example.route_calculator.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.example.route_calculator.model.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.fasterxml.jackson.databind.JsonNode;

public class GeoJsonGraphBuilder {
    private static Map<String, Double> getWeights() {
        Map<String, Double> surfaceWeights = new HashMap<>();
        surfaceWeights.put("asphalt", 1.0);
        surfaceWeights.put("concrete", 1.1);
        surfaceWeights.put("paved", 1.2);
        surfaceWeights.put("gravel", 1.5);
        surfaceWeights.put("dirt", 2.0);
        surfaceWeights.put("sand", 3.0);
        return surfaceWeights;
    }

    private static final double MERGE_DISTANCE_THRESHOLD = 0.002; // 2 meters

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Radius of Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Find or create a node at a given lat/lon
    private static Node findOrCreateNode(String id, double lat, double lon, List<Node> existingNodes) {
        for (Node node : existingNodes) {
            if (haversine(node.lat, node.lon, lat, lon) < MERGE_DISTANCE_THRESHOLD) {
                return node; // Reuse the existing node
            }
        }
        Node newNode = new Node(id, lat, lon);
        existingNodes.add(newNode);
        return newNode;
    }


    public static Graph<Node, DefaultWeightedEdge> buildGraph(JsonNode geoJson) {
        System.out.println("Building graph:");
        long start_time = System.currentTimeMillis();

        Map<String, Double> surfaceWeights = getWeights();
        Graph<Node, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        List<Node> allNodes = Collections.synchronizedList(new ArrayList<>());  // Thread-safe list

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Callable<Void>> tasks = StreamSupport.stream(geoJson.get("features").spliterator(), false)
            .map(feature -> (Callable<Void>) () -> {
                List<Node> segmentNodes = new ArrayList<>();
                String type = feature.get("geometry").get("type").asText();
                JsonNode coordinates = feature.get("geometry").get("coordinates");

                if ("Point".equals(type) || "LineString".equals(type) || "Polygon".equals(type)) {
                    for (JsonNode coord : coordinates) {
                        double lon = coord.get(0).asDouble();
                        double lat = coord.get(1).asDouble();
                        String id = feature.get("properties").get("@id").asText();
                        Node node = findOrCreateNode(id, lat, lon, allNodes);
                        synchronized (graph) {
                            graph.addVertex(node);
                        }
                        segmentNodes.add(node);
                    }
                }

                synchronized (graph) {
                    for (int i = 0; i < segmentNodes.size() - 1; i++) {
                        Node n1 = segmentNodes.get(i);
                        Node n2 = segmentNodes.get(i + 1);
                        if (!n1.equals(n2) && graph.getEdge(n1, n2) == null) {
                            double distance = haversine(n1.lat, n1.lon, n2.lat, n2.lon);
                            DefaultWeightedEdge edge = graph.addEdge(n1, n2);
                            if (edge != null) {
                                graph.setEdgeWeight(edge, distance);
                            }
                        }
                    }
                }
                return null;
            }).collect(Collectors.toList());

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        long end_time = System.currentTimeMillis();
        System.out.println("Finished in " + (end_time - start_time) + "ms");

        return graph;
    }
}
