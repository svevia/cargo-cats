package com.contrast.frontgateservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.contrast.frontgateservice.service.DataServiceProxy;
import com.contrast.frontgateservice.service.UserService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApiControllerSecurityTest {

    @Mock
    private DataServiceProxy dataServiceProxy;

    @Mock
    private UserService userService;

    @InjectMocks
    private ApiController apiController;

    private byte[] createSafeSerializedData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        
        List<Map<String, Object>> addressList = new ArrayList<>();
        Map<String, Object> address = new HashMap<>();
        address.put("fname", "John");
        address.put("name", "Doe");
        address.put("address", "123 Safe Street");
        addressList.add(address);
        
        oos.writeObject(addressList);
        oos.close();
        return baos.toByteArray();
    }

    @Test
    public void testImportAddresses_SafeData() throws Exception {
        // Arrange
        byte[] safeSerializedData = createSafeSerializedData();
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "addresses.ser", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            safeSerializedData
        );
        
        // Act & Assert - This should not throw exception
        assertDoesNotThrow(() -> apiController.importAddresses(file));
    }

    @Test
    public void testImportAddresses_MaliciousData() throws Exception {
        // Arrange - Create a dangerous payload
        byte[] maliciousData = createMaliciousPayload();
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "malicious.ser", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            maliciousData
        );
        
        // Act & Assert - Should throw exception when trying to deserialize malicious data
        assertThrows(
            ClassNotFoundException.class, 
            () -> apiController.importAddresses(file)
        );
    }

    // This creates a serialized object that would be dangerous if deserialized
    // without proper filtering - we're creating a simple class with a custom name
    // that shouldn't be in the whitelist
    private byte[] createMaliciousPayload() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        
        // Write a custom class name that should be blocked by the filter
        oos.writeObject("com.malicious.DangerousObject");
        
        oos.close();
        return baos.toByteArray();
    }
}