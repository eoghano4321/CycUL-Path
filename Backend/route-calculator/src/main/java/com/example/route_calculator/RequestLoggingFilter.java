package com.example.route_calculator;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    @Autowired
    private LastRequestStore lastRequestStore;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            lastRequestStore.setLastRequest((HttpServletRequest) request);
        }
        chain.doFilter(request, response);
    }

    // Other Filter methods (init, destroy) can be left default or implemented as needed
}