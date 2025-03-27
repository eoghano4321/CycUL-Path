package com.example.route_calculator;

import java.io.File;

public class FileCleaner {
    public static void main(String[] args) {
        File file = new File("src/main/resources/SerialisedGraph.json");
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Deleted SerialisedGraph.json successfully.");
            } else {
                System.out.println("Failed to delete SerialisedGraph.json.");
            }
        } else {
            System.out.println("File does not exist, skipping deletion.");
        }
    }
}
