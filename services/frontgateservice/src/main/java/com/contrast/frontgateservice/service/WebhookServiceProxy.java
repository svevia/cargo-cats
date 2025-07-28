package com.contrast.frontgateservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collections;
import java.util.Map;

@Service
public class WebhookServiceProxy {

    @Value("${webhookservice.url:http://webhookservice:5000}")
    private String webhookServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WebhookServiceProxy() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send a webhook notification to a specified URL
     * @param webhookData Map containing 'url' and optional 'method' (GET/POST)
     * @return ResponseEntity with the webhook response
     */
    public ResponseEntity<String> sendWebhookNotification(Map<String, Object> webhookData) {
        try {
            String url = webhookServiceUrl + "/webhookNotify";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String jsonBody = objectMapper.writeValueAsString(webhookData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to send webhook notification: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Test connection to a URL using ping
     * @param connectionData Map containing 'url' to test
     * @return ResponseEntity with the connection test results
     */
    public ResponseEntity<String> testConnection(Map<String, Object> connectionData) {
        try {
            String url = webhookServiceUrl + "/testConnection";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String jsonBody = objectMapper.writeValueAsString(connectionData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to test connection: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Check if the webhook service is running
     * @return ResponseEntity with the health check response
     */
    public ResponseEntity<String> healthCheck() {
        try {
            String url = webhookServiceUrl + "/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to communicate with webhook service: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Send webhook notification with form data (alternative method)
     * @param targetUrl The URL to send webhook to
     * @param method HTTP method (GET or POST)
     * @return ResponseEntity with the webhook response
     */
    public ResponseEntity<String> sendWebhookNotificationForm(String targetUrl, String method) {
        try {
            String url = webhookServiceUrl + "/webhookNotify";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String formData = "url=" + targetUrl;
            if (method != null && !method.isEmpty()) {
                formData += "&method=" + method;
            }
            
            HttpEntity<String> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to send webhook notification: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Test connection with form data (alternative method)
     * @param targetUrl The URL to test connection to
     * @return ResponseEntity with the connection test results
     */
    public ResponseEntity<String> testConnectionForm(String targetUrl) {
        try {
            String url = webhookServiceUrl + "/testConnection";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String formData = "url=" + targetUrl;
            
            HttpEntity<String> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to test connection: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Test shipment notification by sending a test webhook with shipment data
     * @param testData Map containing 'notificationUrl', 'method', and 'shipmentData'
     * @return ResponseEntity with the test result
     */
    public ResponseEntity<String> testShipmentNotification(Map<String, Object> testData) {
        try {
            String url = webhookServiceUrl + "/webhookNotify";
            
            // Create a webhook payload with the shipment data
            String notificationUrl = (String) testData.get("notificationUrl");
            String method = (String) testData.get("method");
            if (method == null || method.isEmpty()) {
                method = "POST"; // Default to POST if not specified
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> shipmentData = (Map<String, Object>) testData.get("shipmentData");
            
            Map<String, Object> webhookPayload = Map.of(
                "url", notificationUrl,
                "method", method,
                "data", Map.of(
                    "event", "shipment_status_test",
                    "shipment", shipmentData,
                    "timestamp", System.currentTimeMillis()
                )
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String jsonBody = objectMapper.writeValueAsString(webhookPayload);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to test shipment notification: " + e.getMessage() + "\"}");
        }
    }
}
