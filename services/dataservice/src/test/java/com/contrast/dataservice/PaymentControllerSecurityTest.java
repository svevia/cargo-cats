package com.contrast.dataservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    @Qualifier("creditCardsJdbcTemplate")
    private JdbcTemplate creditCardsJdbcTemplate;

    @Test
    public void testPaymentEndpointWithValidInputs() throws Exception {
        // Test with valid inputs
        mockMvc.perform(get("/payments")
                .param("creditCard", "4111111111111111")
                .param("shipmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true));

        // Verify that update methods were called with parameters (not direct string concatenation)
        verify(creditCardsJdbcTemplate, times(1)).update(
                eq("INSERT INTO credit_card (card_number, shipment_id) VALUES (?, ?)"),
                eq("4111111111111111"),
                eq(1L));

        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE shipment SET credit_card = ? WHERE id = ?"),
                eq("XXXX-XXXX-XXXX-1111"),
                eq(1L));
    }

    @Test
    public void testPaymentEndpointRejectsSqlInjection() throws Exception {
        // Test SQL injection attempt in creditCard parameter
        mockMvc.perform(get("/payments")
                .param("creditCard", "' OR '1'='1")
                .param("shipmentId", "1"))
                .andExpect(status().isOk());

        // Test SQL injection attempt in shipmentId parameter
        mockMvc.perform(get("/payments")
                .param("creditCard", "4111111111111111")
                .param("shipmentId", "1 OR 1=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].error").value(true));
                
        // Verify that the SQL injection in shipmentId was rejected by our validation
        // and did not reach the database
        verify(jdbcTemplate, times(0)).update(
                anyString(),
                any(),
                eq("1 OR 1=1"));
    }
}