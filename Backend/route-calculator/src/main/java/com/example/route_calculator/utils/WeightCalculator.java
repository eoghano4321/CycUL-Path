package com.example.route_calculator.utils;

import java.util.HashMap;
import java.util.Map;

import com.example.route_calculator.model.Node;

public class WeightCalculator {
    public static double getTurnPenalty(Node prev, Node current, Node next) {
        if (prev == null || current == null || next == null) return 0.0;
    
        // Calculate the change in angle between prev→current and current→next
        double angle1 = Math.atan2(current.lat - prev.lat, current.lon - prev.lon);
        double angle2 = Math.atan2(next.lat - current.lat, next.lon - current.lon);
        double angleDiff = Math.abs(angle1 - angle2);
    
        // Normalize the angle to the range [0, π]
        angleDiff = Math.min(angleDiff, Math.PI * 2 - angleDiff);
    
        // Penalize sharp turns more than straight lines (up to 0.01 for 180-degree turn)
        return 0.2 * (angleDiff / Math.PI);
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
        roadWeights.put("path", 1.0);
        roadWeights.put("track", 1.0);
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
        roadWeights.put("steps", 2.2);
        return roadWeights;
    }

    public static Map<String, Double> getCyclewayWeights() {
        Map<String, Double> cyclewayWeights = new HashMap<>();
        cyclewayWeights.put("separate", 1.0);
        cyclewayWeights.put("track", 1.0);
        cyclewayWeights.put("lane", 1.2);
        cyclewayWeights.put("share_busway", 1.5);
        cyclewayWeights.put("shared_lane", 2.0);
        cyclewayWeights.put("no", 3.0);
        return cyclewayWeights;
    }
}
