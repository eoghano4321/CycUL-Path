package com.example.route_calculator;

import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class LastRequestStore {

    private String lastRequestDetails;

    public void setLastRequest(HttpServletRequest request) {
        System.out.println("Request received: " + request.getRequestURI());
        
        StringBuilder details = new StringBuilder();
        details.append("Method: ").append(request.getMethod()).append("\n");
        details.append("URI: ").append(request.getRequestURI()).append("\n");
        if (request.getQueryString() != null) {
            details.append("Query Params: ").append(request.getQueryString()).append("\n");
        }
        details.append("Headers: \n");
        Collections.list(request.getHeaderNames()).forEach(headerName ->
            details.append("  ").append(headerName).append(": ").append(Collections.list(request.getHeaders(headerName)).stream().collect(Collectors.joining(", "))).append("\n")
        );
        // Add request body if needed, but be cautious as reading the body might consume it
        this.lastRequestDetails = details.toString();
    }

    public String getLastRequestDetails() {
        return lastRequestDetails;
    }
}