package com.example.route_calculator.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.InitializingBean;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.route_calculator.model.GraphResponse;
import com.example.route_calculator.model.Node;
import com.example.route_calculator.utils.GeoJsonGraphBuilder;
import com.example.route_calculator.utils.GeoJsonLoader;
import com.example.route_calculator.utils.GraphSerialiser;
import com.example.route_calculator.utils.IncidentIndex;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GraphService implements InitializingBean {
    private static Graph<Node, DefaultWeightedEdge> graph;

    @Override
    public void afterPropertiesSet() throws Exception {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        loadGraph();
        loadIncidents();
    }

    private void loadGraph() {
        try {
            String filePath = "src/main/resources/SerialisedGraph.json";
            if (Files.exists(Paths.get(filePath))) {
                System.out.println("File exists");
                graph = GraphSerialiser.loadGraphFromFile(filePath);
            } else {
                JsonNode geoJson = GeoJsonLoader.loadGeoJson("src/main/resources/OSM_Dublin_CycleableRoads.geojson");
                if (geoJson == null) {
                    System.err.println("No geojson loaded");
                    throw new IOException("GeoJSON file could not be loaded.");
                }

                System.out.println("File does not exist");
                graph = GeoJsonGraphBuilder.buildGraph(geoJson);
                System.out.println("Graph has " + graph.edgeSet().size() + " edges.");
                GraphSerialiser.saveGraph(getGraphAsJson(), filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load GeoJSON file.");
        }
    }

    private void loadIncidents() {
        try {
            String filePath = "src/main/resources/dublin_incidents_mar2025.geojson";
            if (Files.exists(Paths.get(filePath))) {
                System.out.println("File exists");
                IncidentIndex.loadGeoJSON(filePath);
                double incidentWeight = IncidentIndex.getIncidentWeight(53.349805, 53.350015, -6.25031, -6.26031);
                System.out.println("Demo incident weight: " + incidentWeight);
            } else {
                System.out.println("File does not exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load incident data.");
        }
    }

    public GraphResponse getGraphAsJson() {
        List<Node> nodes = new ArrayList<>(graph.vertexSet());
        List<GraphResponse.EdgeResponse> edges = new ArrayList<>();

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Node source = graph.getEdgeSource(edge);
            Node target = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);

            edges.add(new GraphResponse.EdgeResponse(source, target, weight));
        }

        return new GraphResponse(nodes, edges);
    }

    public static Graph<Node, DefaultWeightedEdge> getGraph(){
        return graph;
    }
}

