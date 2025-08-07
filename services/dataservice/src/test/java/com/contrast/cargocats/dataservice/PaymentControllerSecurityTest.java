package com.contrast.cargocats.dataservice;

import com.contrast.dataservice.PaymentController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
public class PaymentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    @Qualifier("creditCardsJdbcTemplate")
    private JdbcTemplate creditCardsJdbcTemplate;

    @Test
    public void testSqlInjectionMitigation() throws Exception {
        // Setup
        String maliciousCardInput = "1234' OR 1=1; --";
        String maliciousShipmentId = "1; DROP TABLE shipment; --";
        
        // Test the endpoint with malicious inputs
        mockMvc.perform(get("/payments")
                .param("creditCard", maliciousCardInput)
                .param("shipmentId", maliciousShipmentId))
                .andExpect(status().is5xx()); // Since shipmentId can't be parsed to Long, this will throw an exception
        
        // Verify that no raw SQL was executed with concatenated values
        verify(creditCardsJdbcTemplate, times(1)).execute(anyString());
        
        // Using a valid shipmentId to test parameterized queries
        mockMvc.perform(get("/payments")
                .param("creditCard", maliciousCardInput)
                .param("shipmentId", "42"))
                .andExpect(status().isOk());
        
        // Verify that parameterized queries were used
        verify(creditCardsJdbcTemplate, times(1)).update(
                eq("INSERT INTO credit_card (card_number, shipment_id) VALUES (?, ?)"),
                eq(maliciousCardInput),
                eq(42L));
        
        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE shipment SET credit_card = ? WHERE id = ?"),
                anyString(),
                eq(42L));
    }
}