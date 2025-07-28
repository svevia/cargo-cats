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
import java.util.List;
import java.util.ArrayList;

@Service
public class DataServiceProxy {

    @Value("${dataservice.url:http://dataservice:8080}")
    private String dataServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DataServiceProxy() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public ResponseEntity<String> getCats() {
        try {
            String url = dataServiceUrl + "/cats";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to communicate with data service: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> createCat(Map<String, Object> catData) {
        try {
            String url = dataServiceUrl + "/cats";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String jsonBody = objectMapper.writeValueAsString(catData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to create cat: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> deleteCat(String catId) {
        try {
            String url = dataServiceUrl + "/cats/" + catId;
            HttpHeaders headers = new HttpHeaders();
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to delete cat: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getCat(String catId) {
        try {
            String url = dataServiceUrl + "/cats/" + catId;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get cat: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getCatsByOwner(Long ownerId) {
        try {
            String url = dataServiceUrl + "/cats/search/findByObjOwner?objOwner=/users/" + ownerId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to communicate with data service: " + e.getMessage() + "\"}");
        }
    }

    // Shipment tracking methods
    public ResponseEntity<String> getShipmentByTrackingId(String trackingId) {
        try {
            String url = dataServiceUrl + "/shipments/search/findByTrackingId?trackingId=" + trackingId;
            System.out.println("DEBUG: Attempting to call URL: " + url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            System.out.println("DEBUG: Response status: " + response.getStatusCode());
            System.out.println("DEBUG: Response body: " + response.getBody());
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("DEBUG: HTTP Error - Status: " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("DEBUG: General Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get shipment: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getShipmentsByOwner(Long ownerId) {
        try {
            String url = dataServiceUrl + "/shipments/search/findByObjOwner?objOwner=/users/" + ownerId;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get shipments: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> createShipment(Map<String, Object> shipmentData) {
        try {
            String url = dataServiceUrl + "/shipments";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String jsonBody = objectMapper.writeValueAsString(shipmentData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to create shipment: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> updateShipmentStatus(String shipmentId, String status) {
        try {
            // First, get the current shipment data
            String getUrl = dataServiceUrl + "/shipments/" + shipmentId;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> getEntity = new HttpEntity<>(headers);
            ResponseEntity<String> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, getEntity, String.class);
            
            if (!getResponse.getStatusCode().is2xxSuccessful()) {
                return getResponse;
            }
            
            // Parse the current shipment data
            @SuppressWarnings("unchecked")
            Map<String, Object> shipmentData = objectMapper.readValue(getResponse.getBody(), Map.class);
            
            // Update the status
            shipmentData.put("status", status);
            
            // Remove any HAL links that shouldn't be sent back
            shipmentData.remove("_links");
            
            // Send PUT request with complete data
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonBody = objectMapper.writeValueAsString(shipmentData);
            HttpEntity<String> putEntity = new HttpEntity<>(jsonBody, headers);
            
            return restTemplate.exchange(getUrl, HttpMethod.PUT, putEntity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to update shipment status: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> updateShipmentWebhook(String shipmentId, String notificationUrl) {
        try {
            // First, get the current shipment data
            String getUrl = dataServiceUrl + "/shipments/" + shipmentId;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> getEntity = new HttpEntity<>(headers);
            ResponseEntity<String> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, getEntity, String.class);
            
            if (!getResponse.getStatusCode().is2xxSuccessful()) {
                return getResponse;
            }
            
            // Parse the current shipment data
            @SuppressWarnings("unchecked")
            Map<String, Object> shipmentData = objectMapper.readValue(getResponse.getBody(), Map.class);
            
            // Update the notificationUrl
            shipmentData.put("notificationUrl", notificationUrl);
            
            // Remove any HAL links that shouldn't be sent back
            shipmentData.remove("_links");
            
            // Send PUT request with complete data
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonBody = objectMapper.writeValueAsString(shipmentData);
            HttpEntity<String> putEntity = new HttpEntity<>(jsonBody, headers);
            
            return restTemplate.exchange(getUrl, HttpMethod.PUT, putEntity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to update shipment webhook: " + e.getMessage() + "\"}");
        }
    }

    // Address methods
    public ResponseEntity<String> getAddresses() {
        try {
            String url = dataServiceUrl + "/address";
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get addresses: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getAddressesByOwner(Long ownerId) {
        try {
            String url = dataServiceUrl + "/address/search/findByObjOwner?objOwner=/users/" + ownerId;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get addresses: " + e.getMessage() + "\"}");
        }
    }

    // Used for export functionality: returns a List<Object> representing addresses for the user (for demo, List<Map<String,Object>>)
    public List<Object> getAddressesByOwnerAsObjects(Long ownerId) {
        try {
            ResponseEntity<String> response = getAddressesByOwner(ownerId);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return new ArrayList<>();
            }
            String body = response.getBody();
            // Parse JSON: expects { "_embedded": { "address": [ ... ] } }
            Map<String, Object> root = objectMapper.readValue(body, Map.class);
            if (root.containsKey("_embedded")) {
                Map<String, Object> embedded = (Map<String, Object>) root.get("_embedded");
                if (embedded.containsKey("address")) {
                    List<Object> addresses = (List<Object>) embedded.get("address");
                    return addresses;
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("DEBUG: getAddressesByOwnerAsObjects error: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ResponseEntity<String> createAddress(Map<String, Object> addressData) {
        try {
            String url = dataServiceUrl + "/address";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String jsonBody = objectMapper.writeValueAsString(addressData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to create address: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> deleteAddress(String id) {
        try {
            String url = dataServiceUrl + "/address/" + id;
            return restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to delete address: " + e.getMessage() + "\"}");
        }
    }

    // User management methods
    public ResponseEntity<String> createUser(Map<String, Object> userData) {
        try {
            String url = dataServiceUrl + "/user";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            String jsonBody = objectMapper.writeValueAsString(userData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to create user: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getUserByUsername(String username) {
        try {
            String url = dataServiceUrl + "/user/search/findByUsername?username=" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get user: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> checkUserExists(String username) {
        try {
            String url = dataServiceUrl + "/user/search/existsByUsername?username=" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to check user existence: " + e.getMessage() + "\"}");
        }
    }

    // Payment processing method
    public ResponseEntity<String> processPayment(String creditCard, String shipmentId) {
        try {
            String url = dataServiceUrl + "/payments";
            if (creditCard != null && shipmentId != null) {
                url += "?creditCard=" + creditCard + "&shipmentId=" + shipmentId;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to process payment: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getShipmentCat(String shipmentId) {
        try {
            String url = dataServiceUrl + "/shipments/" + shipmentId + "/cat";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get shipment cat: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getShipmentFromAddress(String shipmentId) {
        try {
            String url = dataServiceUrl + "/shipments/" + shipmentId + "/fromAddress";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get shipment from address: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> getShipmentToAddress(String shipmentId) {
        try {
            String url = dataServiceUrl + "/shipments/" + shipmentId + "/toAddress";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get shipment to address: " + e.getMessage() + "\"}");
        }
    }
}
