package com.example.route_calculator.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.*;

public class LoggerTool {
    private static final Logger logger = Logger.getLogger(LoggerTool.class.getName());

    static {
        try {
            // Ensure the logs directory exists
            Files.createDirectories(Paths.get("src/main/logs"));

            // Create FileHandler for logging
            FileHandler fileHandler = new FileHandler("src/main/logs/log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());

            // Remove existing handlers to prevent console logging
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // Add file handler
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Ensures only file logging

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
