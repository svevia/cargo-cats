package com.contrast.dataservice.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String timestamp = LocalDateTime.now().format(formatter);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        System.out.println("=== INCOMING REQUEST ===");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Method: " + method);
        System.out.println("URI: " + uri);
        if (queryString != null && !queryString.isEmpty()) {
            System.out.println("Query String: " + queryString);
        }
        System.out.println("Remote Address: " + remoteAddr);
        if (userAgent != null) {
            System.out.println("User-Agent: " + userAgent);
        }
        System.out.println("========================");
        
        return true; // Continue with the request
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String timestamp = LocalDateTime.now().format(formatter);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        
        System.out.println("=== REQUEST COMPLETED ===");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Method: " + method);
        System.out.println("URI: " + uri);
        System.out.println("Response Status: " + status);
        if (ex != null) {
            System.out.println("Exception: " + ex.getMessage());
        }
        System.out.println("=========================");
    }
}
