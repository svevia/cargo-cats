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
public class LabelServiceProxy {

    @Value("${labelservice.url:http://labelservice:3000}")
    private String labelServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LabelServiceProxy() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate a PDF shipping label
     * @param labelData Map containing 'firstName', 'lastName', 'address', and optional 'trackingNumber'
     * @return ResponseEntity with the PDF bytes
     */
    public ResponseEntity<byte[]> generateLabel(Map<String, Object> labelData) {
        try {
            String url = labelServiceUrl + "/generate-label";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_PDF));
            
            String jsonBody = objectMapper.writeValueAsString(labelData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Check label service health
     * @return ResponseEntity with health status
     */
    public ResponseEntity<String> healthCheck() {
        try {
            String url = labelServiceUrl + "/health";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Unable to connect to label service\"}");
        }
    }

    /**
     * Get API example/documentation
     * @return ResponseEntity with API documentation
     */
    public ResponseEntity<String> getExample() {
        try {
            String url = labelServiceUrl + "/example";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Unable to connect to label service\"}");
        }
    }
}
