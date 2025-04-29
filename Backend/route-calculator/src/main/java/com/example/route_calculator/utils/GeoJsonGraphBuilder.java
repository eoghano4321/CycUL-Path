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
import java.util.concurrent.ConcurrentHashMap; // Use ConcurrentHashMap for better concurrency

import org.jgrapht.graph.DefaultWeightedEdge;

import com.example.route_calculator.model.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.fasterxml.jackson.databind.JsonNode;

public class GeoJsonGraphBuilder {
    private static final double MERGE_DISTANCE_THRESHOLD = 0.001; // ~100m, adjust as needed
    private static final double GRID_SIZE = 0.001; // Grid size for spatial indexing, ~100m

    // Helper function to generate grid keys (can be static as it's stateless)
    private static String getGridKey(double lat, double lon, double gridSize) {
        int latIndex = (int) (lat / gridSize);
        int lonIndex = (int) (lon / gridSize);
        return latIndex + "," + lonIndex;
    }

    private static List<Node> getNodesFromNeighbors(double lat, double lon, double gridSize, Map<String, List<Node>> spatialGrid) {
        List<Node> neighbors = new ArrayList<>();
        int latIndex = (int) (lat / gridSize);
        int lonIndex = (int) (lon / gridSize);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                String neighborKey = (latIndex + i) + "," + (lonIndex + j);
                // Use getOrDefault with an empty list to handle missing keys safely
                List<Node> nodesInCell = spatialGrid.getOrDefault(neighborKey, Collections.emptyList());
                
                 neighbors.addAll(nodesInCell);
            }
        }
        return neighbors;
    }

     private static Node findOrCreateNode(String id, double lat, double lon, Graph<Node, DefaultWeightedEdge> graph, List<Node> allNodes, Map<String, List<Node>> spatialGrid, double gridSize) {
        List<Node> candidateNodes = getNodesFromNeighbors(lat, lon, gridSize, spatialGrid);

        List<Node> localCandidates = new ArrayList<>(candidateNodes); // Copy for safe iteration

        for (Node node : localCandidates) {
            // Check distance only against candidates
            if (DistanceCalculator.haversine(lat, lon, node.lat, node.lon) < MERGE_DISTANCE_THRESHOLD) {
                return node;
            }
        }


        Node newNode = new Node(id, lat, lon);

        // 4. Add the new node safely to shared structures
        // Add to the main list (already synchronized externally or is thread-safe)
        allNodes.add(newNode);

        // Add vertex to the graph (synchronized externally or is thread-safe)
        synchronized (graph) {
             graph.addVertex(newNode);
        }


        // Add to the spatial grid
        String gridKey = getGridKey(lat, lon, gridSize);
        // Use computeIfAbsent for atomic creation of the list for a key.
        // The list itself should be thread-safe (e.g., CopyOnWriteArrayList or synchronizedList).
        // Using Collections.synchronizedList here.
        List<Node> nodesInCell = spatialGrid.computeIfAbsent(gridKey, k -> Collections.synchronizedList(new ArrayList<>()));
        nodesInCell.add(newNode); // The add operation on the synchronizedList is thread-safe.


        return newNode;
    }

    public static Graph<Node, DefaultWeightedEdge> buildGraph(JsonNode geoJson) {
        System.out.println("Building graph:");
        long start_time = System.currentTimeMillis();

        Map<String, Double> surfaceWeights = WeightCalculator.getSurfaceWeights();
        Map<String, Double> roadWeights = WeightCalculator.getRoadWeights();
        Map<String, Double> cyclewayWeights = WeightCalculator.getCyclewayWeights();

        Graph<Node, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        List<Node> allNodes = Collections.synchronizedList(new ArrayList<>()); // Thread-safe list
        // Use ConcurrentHashMap for the spatial grid - generally better performance under high concurrency
        Map<String, List<Node>> spatialGrid = new ConcurrentHashMap<>();


        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Callable<Void>> tasks = StreamSupport.stream(geoJson.get("features").spliterator(), true)
            .map(feature -> (Callable<Void>) () -> {
                List<Node> segmentNodes = new ArrayList<>();
                String type = feature.get("geometry").get("type").asText();
                JsonNode coordinates = feature.get("geometry").get("coordinates");

                if ("Point".equals(type) || "LineString".equals(type) || "Polygon".equals(type)) {
                    // Handle nested coordinates for Polygon/MultiLineString if necessary
                    JsonNode effectiveCoordinates = coordinates;
                     if ("Polygon".equals(type)) {
                         // Polygons have an extra level of nesting for the exterior ring
                         // Check if coordinates is an array and not empty before accessing index 0
                         if (coordinates != null && coordinates.isArray() && coordinates.size() > 0) {
                            effectiveCoordinates = coordinates.get(0);
                         } else {
                             // Handle cases where Polygon coordinates might be invalid or empty
                             System.err.println("Warning: Invalid or empty coordinates for Polygon feature ID: " + feature.path("properties").path("@id").asText("N/A"));
                             effectiveCoordinates = null; // Skip processing this feature's geometry
                         }
                     }
                     // Add handling for MultiLineString, MultiPolygon if they exist in data

                    if (effectiveCoordinates != null) { // Process only if coordinates are valid
                        for (JsonNode coord : effectiveCoordinates) {
                            // Check if coord is valid (e.g., has 2 elements)
                            if (coord != null && coord.isArray() && coord.size() >= 2) {
                                double lon = coord.get(0).asDouble();
                                double lat = coord.get(1).asDouble();
                                // Use path() for safer access to potentially missing properties
                                String id = feature.path("properties").path("@id").asText("default_id_" + System.nanoTime()); // Provide a default ID

                                // Pass the spatial grid and grid size to findOrCreateNode
                                Node node = findOrCreateNode(id, lat, lon, graph, allNodes, spatialGrid, GRID_SIZE);

                                segmentNodes.add(node);
                            } else {
                                System.err.println("Warning: Invalid coordinate format in feature ID: " + feature.path("properties").path("@id").asText("N/A"));
                            }
                        }
                    }
                }

                // Add edges for the current feature (segment)
                // This part needs synchronization as it modifies the shared graph structure
                synchronized (graph) {
                    for (int i = 0; i < segmentNodes.size() - 1; i++) {
                        Node n1 = segmentNodes.get(i);
                        Node n2 = segmentNodes.get(i + 1);

                        // Check if edge exists before adding and calculating weight
                        if (!n1.equals(n2) && !graph.containsEdge(n1, n2)) {
                            double distance = DistanceCalculator.haversine(n1.lat, n1.lon, n2.lat, n2.lon);

                            // Calculate weight based on feature properties using path() for safety
                            String surface = feature.path("properties").path("surface").asText(null);
                            String cycleway = feature.path("properties").path("cycleway").asText(null);
                            String highway = feature.path("properties").path("highway").asText(null);

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
                } // End synchronized block for graph modification
                return null;
            }).collect(Collectors.toList());

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            System.err.println("Graph building interrupted: " + e.getMessage());
            // Handle interruption appropriately, maybe return partial graph or throw exception
        } finally {
             executor.shutdown();
        }


        // Connect nearby nodes *after* all initial nodes and edges are processed
        // Pass a copy of allNodes to avoid potential concurrent modification issues
        // if allNodes were still being modified elsewhere (though it shouldn't be after executor shutdown).
        connectNearbyNodes(graph, new ArrayList<>(allNodes));

        long end_time = System.currentTimeMillis();
        System.out.println("Finished building graph in " + (end_time - start_time) + "ms");

        return graph;
    }

    private static void connectNearbyNodes(Graph<Node, DefaultWeightedEdge> graph, List<Node> nodesList) {
        System.out.println("\nConnecting nearby nodes..."); // Fixed the newline character in the string
        long connect_start_time = System.currentTimeMillis();

        // Step 1: Create a spatial index (grid-based lookup) for *this specific step*
        // It's rebuilt here to ensure it reflects the final state of allNodes accurately.
        Map<String, List<Node>> connectGrid = new HashMap<>();

        for (Node node : nodesList) {
            String key = getGridKey(node.lat, node.lon, GRID_SIZE);
            connectGrid.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
        }

        // Step 2: Compare nodes within the same or neighboring grid cells
        // Synchronize graph modifications
        synchronized (graph) {
            int edgesAdded = 0;
            for (String key : connectGrid.keySet()) {
                List<Node> cellNodes = connectGrid.get(key);
                // Use the static helper, passing the grid built for *this* connection phase
                List<Node> nodesToCheck = getNodesFromNeighborsByKey(key, connectGrid);


                for (Node n1 : cellNodes) {
                    for (Node n2 : nodesToCheck) {
                        // Avoid self-loops and redundant checks/edges
                        if (!n1.equals(n2) && !graph.containsEdge(n1, n2)) {
                            double distance = DistanceCalculator.haversine(n1.lat, n1.lon, n2.lat, n2.lon);
                            if (distance < MERGE_DISTANCE_THRESHOLD) {
                                DefaultWeightedEdge edge = graph.addEdge(n1, n2);
                                if (edge != null) {
                                    // Use a small weight, potentially just the distance,
                                    // as these represent close proximity connections.
                                    graph.setEdgeWeight(edge, distance);
                                    edgesAdded++;
                                }
                            }
                        }
                    }
                }
            }
             System.out.println("Added " + edgesAdded + " proximity edges.");
        } // End synchronized block

        long connect_end_time = System.currentTimeMillis();
        System.out.println("Finished connecting nearby nodes in " + (connect_end_time - connect_start_time) + "ms");
    }


    // Helper function to get nodes from neighboring grid cells using a key
    // Used by connectNearbyNodes
    private static List<Node> getNodesFromNeighborsByKey(String key, Map<String, List<Node>> spatialGrid) {
        List<Node> neighbors = new ArrayList<>();
        String[] parts = key.split(",");
        if (parts.length != 2) return Collections.emptyList(); // Invalid key format

        try {
            int latIndex = Integer.parseInt(parts[0]);
            int lonIndex = Integer.parseInt(parts[1]);

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    String neighborKey = (latIndex + i) + "," + (lonIndex + j);
                    neighbors.addAll(spatialGrid.getOrDefault(neighborKey, Collections.emptyList()));
                }
            }
        } catch (NumberFormatException e) {
             System.err.println("Error parsing grid key: " + key);
             return Collections.emptyList(); // Return empty list on error
        }
        return neighbors;
    }
}