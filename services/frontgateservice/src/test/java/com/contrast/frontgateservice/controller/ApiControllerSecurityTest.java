package com.contrast.frontgateservice.controller;

import com.contrast.frontgateservice.entity.User;
import com.contrast.frontgateservice.service.DataServiceProxy;
import com.contrast.frontgateservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ApiControllerSecurityTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private DataServiceProxy dataServiceProxy;

    @InjectMocks
    private ApiController apiController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(apiController).build();
        
        // Setup security context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        
        // Setup user service
        User mockUser = new User();
        mockUser.setId("1");
        mockUser.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(mockUser);
    }

    @Test
    public void testImportAddresses_withSafeData() throws Exception {
        // Create a safe serialized object (List of Maps)
        byte[] safeSerializedData = createSafeSerializedData();
        
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "addresses.ser", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, 
            safeSerializedData
        );
        
        // Mock the data service response
        when(dataServiceProxy.createAddress(any(Map.class))).thenReturn(
            org.springframework.http.ResponseEntity.ok().body("{\"id\":\"123\"}")
        );
        
        // Perform the request
        mockMvc.perform(multipart("/api/addresses/import")
                .file(file))
                .andExpect(status().isOk());
    }
    
    /**
     * Helper method to create a safe serialized object for testing
     */
    private byte[] createSafeSerializedData() throws IOException {
        List<Map<String, Object>> addresses = new ArrayList<>();
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "Anytown");
        address.put("state", "CA");
        address.put("zip", "12345");
        addresses.add(address);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(addresses);
        oos.close();
        
        return baos.toByteArray();
    }
}