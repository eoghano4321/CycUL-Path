package com.example.route_calculator.utils;
import org.locationtech.jts.index.strtree.STRtree;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Envelope;

public class IncidentIndex {
    private static final STRtree rtree = new STRtree();
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static void loadGeoJSON(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(Paths.get(filePath).toFile());

        // Iterate through features
        for (JsonNode feature : root.get("features")) {
            JsonNode geometry = feature.get("geometry");
            JsonNode properties = feature.get("properties");

            if (geometry != null && "Point".equals(geometry.get("type").asText())) {
                // Extract coordinates
                double lon = geometry.get("coordinates").get(0).asDouble();
                double lat = geometry.get("coordinates").get(1).asDouble();
                double severityScore = properties.get("Severity").asDouble();

                // Insert into R-tree
                addIncident(lat, lon, severityScore);
            }
        }
    }

    // Insert accident data
    private static void addIncident(double lat, double lon, double severity) {
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        rtree.insert(point.getEnvelopeInternal(), severity);
    }

    // Query incidents near a given segment
    public static double getIncidentWeight(double minLat, double maxLat, double minLon, double maxLon) {
        // adjust the search area slightly to ensure it includes the edges of the segment
        minLat -=0.00005;
        maxLat +=0.00005;
        minLon -=0.00005;
        maxLon +=0.00005;
        Envelope searchArea = new Envelope(minLon, maxLon, minLat, maxLat);
        try {
            Double queryResult = rtree.query(searchArea).stream().mapToDouble(result -> (double) result).sum();
            return queryResult; // Sum the severity scores of all incidents in the area
        } catch (Exception e) {
            System.err.println("Error building R-tree: " + e.getMessage());
            e.printStackTrace();
            return 0.0; // Return 0 if an error occurs
        }
    }
}
