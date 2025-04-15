package com.example.route_calculator.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.example.route_calculator.model.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.fasterxml.jackson.databind.JsonNode;

public class GeoJsonGraphBuilder {
    private static final double MERGE_DISTANCE_THRESHOLD = 0.001; 
    private static Logger logger = LoggerTool.getLogger();

    // Find or create a node at a given lat/lon
    private static Node findOrCreateNode(String id, double lat, double lon, Graph<Node, DefaultWeightedEdge> graph, List<Node> existingNodes) {
        synchronized (existingNodes) {
            for (Node node : existingNodes) {
                if (DistanceCalculator.haversine(lat, lon, node.lat, node.lon) < MERGE_DISTANCE_THRESHOLD) {
                    return node; // Return an existing node if within threshold
                }
            }
    
            // If no close node is found, create a new one
            Node newNode = new Node(id, lat, lon);
            existingNodes.add(newNode);
    
            // Ensure the node is added to the graph safely
            synchronized (graph) {
                graph.addVertex(newNode);
            }
    
            return newNode;
        }
    }
    
    public static Graph<Node, DefaultWeightedEdge> buildGraph(JsonNode geoJson) {
        logger.info("Building graph:");
        long start_time = System.currentTimeMillis();

        Map<String, Double> surfaceWeights = WeightCalculator.getSurfaceWeights();
        Map<String, Double> roadWeights = WeightCalculator.getRoadWeights();
        Map<String, Double> cyclewayWeights = WeightCalculator.getCyclewayWeights();
        
        Graph<Node, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        List<Node> allNodes = Collections.synchronizedList(new ArrayList<>());  // Thread-safe list

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());  // Adjust the number of threads as needed

        List<Callable<Void>> tasks = StreamSupport.stream(geoJson.get("features").spliterator(), true)
            .map(feature -> (Callable<Void>) () -> {
                List<Node> segmentNodes = new ArrayList<>();
                String type = feature.get("geometry").get("type").asText();
                JsonNode coordinates = feature.get("geometry").get("coordinates");

                if ("Point".equals(type) || "LineString".equals(type) || "Polygon".equals(type)) {
                    for (JsonNode coord : coordinates) {
                        double lon = coord.get(0).asDouble();
                        double lat = coord.get(1).asDouble();
                        String id = feature.get("properties").get("@id").asText();
                        Node node = findOrCreateNode(id, lat, lon, graph, allNodes);
                        synchronized (graph) {
                            graph.addVertex(node);
                        }
                        segmentNodes.add(node);
                    }
                }

                synchronized (graph) {
                    for (Node node : segmentNodes) {
                        graph.addVertex(node);  // Add all segment nodes in one go
                    }
        
                    for (int i = 0; i < segmentNodes.size() - 1; i++) {
                        Node n1 = segmentNodes.get(i);
                        Node n2 = segmentNodes.get(i + 1);
                        
                        if (!graph.containsEdge(n1, n2)) {  // Avoid redundant edge lookups
                            double distance = DistanceCalculator.haversine(n1.lat, n1.lon, n2.lat, n2.lon);

                            String surface = feature.get("properties").has("surface") ? feature.get("properties").get("surface").asText() : null;
                            String cycleway = feature.get("properties").has("cycleway") ? feature.get("properties").get("cycleway").asText() : null;
                            String highway = feature.get("properties").has("highway") ? feature.get("properties").get("highway").asText() : null;

                            double surfaceWeight = surface != null && surfaceWeights.containsKey(surface) ? surfaceWeights.get(surface) : 1.0;
                            double cyclewayWeight = cycleway != null && cyclewayWeights.containsKey(cycleway) ? cyclewayWeights.get(cycleway) : 1.0;
                            double roadWeight = highway != null && roadWeights.containsKey(highway) ? roadWeights.get(highway) : 1.0;

                            double weightedDistance = distance * surfaceWeight * cyclewayWeight * roadWeight;
                            DefaultWeightedEdge edge = graph.addEdge(n1, n2);
                            if (edge != null) {
                                graph.setEdgeWeight(edge, weightedDistance);
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

        connectNearbyNodes(graph, allNodes);

        long end_time = System.currentTimeMillis();
        logger.info("Finished in " + (end_time - start_time) + "ms");

        return graph;
    }

    private static void connectNearbyNodes(Graph<Node, DefaultWeightedEdge> graph, List<Node> allNodes) {
        logger.info("\nConnecting nearby nodes...");

        // Step 1: Create a spatial index (grid-based lookup)
        Map<String, List<Node>> spatialGrid = new HashMap<>();
        double GRID_SIZE = 0.001; // ~100m

        for (Node node : allNodes) {
            String key = getGridKey(node.lat, node.lon, GRID_SIZE);
            spatialGrid.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
        }

        // Step 2: Only compare nodes within the same or neighboring grid cells
        synchronized (graph) {
            for (String key : spatialGrid.keySet()) {
                List<Node> cellNodes = spatialGrid.get(key);
                List<Node> neighbors = getNeighboringCells(key, spatialGrid);

                for (Node n1 : cellNodes) {
                    for (Node n2 : neighbors) {
                        if (!n1.equals(n2) && graph.getEdge(n1, n2) == null) {
                            double distance = DistanceCalculator.haversine(n1.lat, n1.lon, n2.lat, n2.lon);
                            if (distance < MERGE_DISTANCE_THRESHOLD) {
                                DefaultWeightedEdge edge = graph.addEdge(n1, n2);
                                if (edge != null) {
                                    graph.setEdgeWeight(edge, distance);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper function to generate grid keys
    private static String getGridKey(double lat, double lon, double gridSize) {
        int latIndex = (int) (lat / gridSize);
        int lonIndex = (int) (lon / gridSize);
        return latIndex + "," + lonIndex;
    }

    // Helper function to get nodes from neighboring grid cells
    private static List<Node> getNeighboringCells(String key, Map<String, List<Node>> spatialGrid) {
        List<Node> neighbors = new ArrayList<>();
        String[] parts = key.split(",");
        int latIndex = Integer.parseInt(parts[0]);
        int lonIndex = Integer.parseInt(parts[1]);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                String neighborKey = (latIndex + i) + "," + (lonIndex + j);
                if (spatialGrid.containsKey(neighborKey)) {
                    neighbors.addAll(spatialGrid.get(neighborKey));
                }
            }
        }
        return neighbors;
    }
}