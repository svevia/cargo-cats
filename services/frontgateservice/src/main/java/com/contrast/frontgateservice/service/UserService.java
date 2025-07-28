package com.contrast.frontgateservice.service;

import com.contrast.frontgateservice.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private DataServiceProxy dataServiceProxy;
    
    // INSECURE: Using MD5 for educational purposes to demonstrate weak password storage
    // MD5 is cryptographically broken and should NEVER be used in production
    @SuppressWarnings("deprecation") // Intentionally using deprecated MD5 for educational demo
    private final MessageDigestPasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("MD5");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("deprecation") // Intentionally using deprecated MD5 for educational demo
    public User createUser(String username, String password) throws Exception {
        // Check if user already exists
        if (userExists(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // Create user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", passwordEncoder.encode(password));
        userData.put("enabled", true);

        // Call dataservice to create user
        ResponseEntity<String> response = dataServiceProxy.createUser(userData);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            // Parse the response to get the created user
            JsonNode userNode = objectMapper.readTree(response.getBody());
            
            User user = new User();
            user.setId(userNode.get("id").asLong());
            user.setUsername(userNode.get("username").asText());
            user.setPassword(userNode.get("password").asText());
            user.setEnabled(userNode.get("enabled").asBoolean());
            
            return user;
        } else {
            throw new RuntimeException("Failed to create user: " + response.getBody());
        }
    }

    public User findByUsername(String username) {
        try {
            ResponseEntity<String> response = dataServiceProxy.getUserByUsername(username);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                
                // Check if we have embedded users (from search endpoint)
                if (rootNode.has("_embedded") && rootNode.get("_embedded").has("user")) {
                    JsonNode usersArray = rootNode.get("_embedded").get("user");
                    if (usersArray.isArray() && usersArray.size() > 0) {
                        JsonNode userNode = usersArray.get(0);
                        return parseUserFromJson(userNode);
                    }
                } else if (rootNode.has("id")) {
                    // Direct user response
                    return parseUserFromJson(rootNode);
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error finding user by username: " + e.getMessage());
            return null;
        }
    }

    public boolean userExists(String username) {
        try {
            ResponseEntity<String> response = dataServiceProxy.checkUserExists(username);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse boolean response
                return Boolean.parseBoolean(response.getBody());
            }
            
            // If the existsByUsername endpoint doesn't exist, fallback to checking if findByUsername returns a user
            return findByUsername(username) != null;
        } catch (Exception e) {
            // Fallback to checking if findByUsername returns a user
            return findByUsername(username) != null;
        }
    }

    private User parseUserFromJson(JsonNode userNode) {
        User user = new User();
        user.setId(userNode.get("id").asLong());
        user.setUsername(userNode.get("username").asText());
        user.setPassword(userNode.get("password").asText());
        user.setEnabled(userNode.get("enabled").asBoolean());
        return user;
    }
}
