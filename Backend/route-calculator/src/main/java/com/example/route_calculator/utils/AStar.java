package com.example.route_calculator.utils;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.LoggerFactory;

import com.example.route_calculator.model.Node;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AStar {
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

        AStarShortestPath<Node, DefaultWeightedEdge> astar = new AStarShortestPath<>(graph, (n1, n2) -> 
            GeoJsonGraphBuilder.haversine(n1.lat, n1.lon, n2.lat, n2.lon)
        );
        logger.info("Start Node: " + startNode);
        logger.info("End Node: " + endNode);
        
        if (astar.getPath(startNode, endNode) == null) {
            throw new IllegalArgumentException("No path found between the start and end nodes.");  
        }
        
        logger.info("A* Path: " + astar.getPath(startNode, endNode));

        return astar.getPath(startNode, endNode).getVertexList();
    }

    private Node findClosestNode(double lat, double lon) {
        return graph.vertexSet().stream()
                .min((n1, n2) -> Double.compare(
                        GeoJsonGraphBuilder.haversine(n1.lat, n1.lon, lat, lon),
                        GeoJsonGraphBuilder.haversine(n2.lat, n2.lon, lat, lon)))
                .orElse(null);
    }
}
