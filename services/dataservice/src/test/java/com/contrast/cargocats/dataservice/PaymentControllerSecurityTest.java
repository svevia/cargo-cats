package com.contrast.cargocats.dataservice;

import com.contrast.dataservice.PaymentController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
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
    public void testSqlInjectionPrevention() throws Exception {
        // Setup
        when(creditCardsJdbcTemplate.update(anyString(), any(), any())).thenReturn(1);
        when(jdbcTemplate.update(anyString(), any(), any())).thenReturn(1);

        // Test with normal input
        mockMvc.perform(get("/payments")
                .param("creditCard", "4111111111111111")
                .param("shipmentId", "123"))
                .andExpect(status().isOk());

        // Verify that parameterized queries are used
        verify(creditCardsJdbcTemplate, times(1)).update(
                eq("INSERT INTO credit_card (card_number, shipment_id) VALUES (?, ?)"),
                eq("4111111111111111"),
                eq("123"));

        // Test with SQL injection attempt
        mockMvc.perform(get("/payments")
                .param("creditCard", "' OR '1'='1")
                .param("shipmentId", "123; DROP TABLE credit_card;--"))
                .andExpect(status().isOk());

        // Verify that the SQL injection attempt is safely parameterized
        verify(creditCardsJdbcTemplate, times(1)).update(
                eq("INSERT INTO credit_card (card_number, shipment_id) VALUES (?, ?)"),
                eq("' OR '1'='1"),
                eq("123; DROP TABLE credit_card;--"));
    }
}