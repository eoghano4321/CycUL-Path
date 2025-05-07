package com.example.route_calculator.utils;

import java.util.HashMap;
import java.util.Map;

import com.example.route_calculator.model.Node;

public class WeightCalculator {
    public static double getTurnPenalty(Node prev, Node current, Node next) {
        if (prev == null || current == null || next == null) return 0.0;

        // Calculate vectors for the segments
        double v1x = current.lon - prev.lon;
        double v1y = current.lat - prev.lat;
        double v2x = next.lon - current.lon;
        double v2y = next.lat - current.lat;

        // Calculate magnitudes
        double mag1 = Math.hypot(v1x, v1y);
        double mag2 = Math.hypot(v2x, v2y);

        // Avoid division by zero if a segment has zero length
        if (mag1 == 0 || mag2 == 0) {
            return 0.0; 
        }

        // Calculate dot product
        double dotProduct = v1x * v2x + v1y * v2y;

        // Calculate cosine of the angle
        double cosTheta = dotProduct / (mag1 * mag2);

        // Clamp cosTheta to [-1, 1] to handle potential floating-point inaccuracies
        cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));

        // Calculate penalty: 0 for straight (cosTheta=1), max penalty for U-turn (cosTheta=-1)
        // Using 0.1 as the factor 'k' to make the max penalty (1 - (-1)) * 0.1 = 0.2, matching the previous max.
        return 0.1 * (1.0 - cosTheta);
    }
    
    public static double getIncidentWeight(Node n1, Node n2) {
        double minLat = Math.min(n1.lat, n2.lat);
        double maxLat = Math.max(n1.lat, n2.lat);
        double minLon = Math.min(n1.lon, n2.lon);
        double maxLon = Math.max(n1.lon, n2.lon);
        return IncidentIndex.getIncidentWeight(minLat, maxLat, minLon, maxLon);
    }

    public static Map<String, Double> getSurfaceWeights() {
        Map<String, Double> surfaceWeights = new HashMap<>();
        surfaceWeights.put("asphalt", 1.0);
        surfaceWeights.put("concrete", 1.1);
        surfaceWeights.put("paved", 1.2);
        surfaceWeights.put("gravel", 1.5);
        surfaceWeights.put("dirt", 2.0);
        surfaceWeights.put("sand", 3.0);
        return surfaceWeights;
    }

    public static Map<String, Double> getRoadWeights() {
        Map<String, Double> roadWeights = new HashMap<>();
        roadWeights.put("cycleway", 1.0);
        roadWeights.put("track", 1.0);
        roadWeights.put("path", 1.2);
        roadWeights.put("footway", 1.2);
        roadWeights.put("living_street", 1.2);
        roadWeights.put("pedestrian", 1.2);
        roadWeights.put("tertiary", 1.5);
        roadWeights.put("tertiary_link", 1.5);
        roadWeights.put("residential", 1.5);
        roadWeights.put("service", 1.5);
        roadWeights.put("unclassified", 1.5);
        roadWeights.put("bridleway", 1.5);
        roadWeights.put("secondary", 1.8);
        roadWeights.put("secondary_link", 1.8);
        roadWeights.put("primary", 2.0);
        roadWeights.put("primary_link", 2.0);
        roadWeights.put("trunk", 2.0);
        roadWeights.put("trunk_link", 2.0);
        roadWeights.put("steps", 3.0);
        return roadWeights;
    }

    public static Map<String, Double> getCyclewayWeights() {
        Map<String, Double> cyclewayWeights = new HashMap<>();
        cyclewayWeights.put("separate", 1.0);
        cyclewayWeights.put("track", 1.0);
        cyclewayWeights.put("lane", 1.2);
        cyclewayWeights.put("share_busway", 1.4);
        cyclewayWeights.put("shared_lane", 1.5);
        cyclewayWeights.put("no", 2.0);
        return cyclewayWeights;
    }
}
