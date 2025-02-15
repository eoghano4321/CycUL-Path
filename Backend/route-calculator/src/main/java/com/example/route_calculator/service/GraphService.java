package com.example.route_calculator.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.InitializingBean;
import java.io.IOException;
import com.example.route_calculator.model.*;
import com.example.route_calculator.utils.GeoJsonGraphBuilder;
import com.example.route_calculator.utils.GeoJsonLoader;

@Service
public class GraphService implements InitializingBean {
    private Graph graph;

    @Override
    public void afterPropertiesSet() throws Exception {
        loadGraph();
    }

    private void loadGraph() {
        try {
            JsonNode geoJson = GeoJsonLoader.loadGeoJson("src/main/resources/CombinedDublinCycleNetwork.geojson");
            graph = GeoJsonGraphBuilder.buildGraph(geoJson);
            System.out.println("Graph successfully loaded with " + graph.getNodes().size() + " nodes.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load GeoJSON file.");
        }
    }

    public Graph getGraph() {
        return graph;
    }
}

