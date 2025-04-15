package com.example.route_calculator.utils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.example.route_calculator.model.GraphResponse;
import com.example.route_calculator.model.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


public class GraphSerialiser {
    private static Logger logger = LoggerTool.getLogger();
    public static void saveGraph(GraphResponse graph, String filePath){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(filePath), graph);
            logger.info("Graph saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Graph<Node, DefaultWeightedEdge> loadGraphFromFile(String filePath){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GraphResponse graphResponse = objectMapper.readValue(new File(filePath), GraphResponse.class);
            return convertToGraph(graphResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Graph<Node, DefaultWeightedEdge> convertToGraph(GraphResponse graphResponse) {
        Graph<Node, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Add nodes
        for (Node node : graphResponse.getNodes()) {
            graph.addVertex(node);
        }

        // Add edges
        for (GraphResponse.EdgeResponse edgeResponse : graphResponse.getEdges()) {
            Node source = edgeResponse.getSource();
            Node target = edgeResponse.getTarget();
            double weight = edgeResponse.getWeight();

            DefaultWeightedEdge edge = graph.addEdge(source, target);
            if (edge != null) {
                graph.setEdgeWeight(edge, weight);
            }
        }

        return graph;
    }
}
