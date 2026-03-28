package com.contrast.dataservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "*")
public class PaymentController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    @Qualifier("creditCardsJdbcTemplate")
    private JdbcTemplate creditCardsJdbcTemplate;
    
    @GetMapping("/payments")
    public List<Map<String, Object>> executeRawQuery(
            @RequestParam(value = "creditCard", required = false) String creditCard,
            @RequestParam(value = "shipmentId", required = false) String shipmentId) {
        
        System.out.println("=== PAYMENT ENDPOINT REQUEST ===");
        System.out.println("Endpoint: /payments");
        System.out.println("Credit Card param: " + (creditCard != null ? creditCard : "null"));
        System.out.println("Shipment ID param: " + (shipmentId != null ? shipmentId : "null"));
        System.out.println("================================");
        
        try {
            List<Map<String, Object>> result = null;
            
            // entities are too hard. cant figure em out. im just gonna use raw sql
            if (creditCard != null && !creditCard.isEmpty() && shipmentId != null && !shipmentId.isEmpty()) {
                // Create credit card table if it doesn't exist in the credit_cards database
                String createTableSql = "CREATE TABLE IF NOT EXISTS credit_card (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "card_number VARCHAR(255) NOT NULL, " +
                    "shipment_id BIGINT NOT NULL)";
                creditCardsJdbcTemplate.execute(createTableSql);
                
                // Insert credit card data into the credit_cards database using prepared statement
                String insertSql = "INSERT INTO credit_card (card_number, shipment_id) VALUES (?, ?)";
                System.out.println("DEBUG: Executing SQL statement with prepared statement on credit_cards database");
                System.out.println("DEBUG: Credit Card parameter: " + creditCard);
                System.out.println("DEBUG: Shipment ID parameter: " + shipmentId);
                
                // Execute the insert statement using the creditCardsJdbcTemplate with parameters
                System.out.println("DEBUG: Using creditCardsJdbcTemplate to execute query on credit_cards database");
                creditCardsJdbcTemplate.update(insertSql, creditCard, shipmentId);
                
                // Update the main shipment table to reference the credit card (but not store the actual number)
                String maskedCreditCard = "XXXX-XXXX-XXXX-" + 
                    (creditCard.length() > 4 ? creditCard.substring(creditCard.length() - 4) : creditCard);
                
                String updateSql = "UPDATE shipment SET credit_card = ? WHERE id = ?";
                
                System.out.println("DEBUG: Using main jdbcTemplate to execute query on main database");
                
                // Execute the update statement using the default jdbcTemplate with parameters
                jdbcTemplate.update(updateSql, maskedCreditCard, shipmentId);
                
                // Create response with success message
                result = List.of(Map.of(
                    "success", true,
                    "message", "Credit card stored in separate database for shipment",
                    "shipment_id", shipmentId,
                    "credit_card", maskedCreditCard
                ));
            } else {
                result = List.of(Map.of(
                    "error", true,
                    "message", "Both creditCard and shipmentId parameters are required for payment processing",
                    "credit_card_param", creditCard != null ? creditCard : "none",
                    "shipment_id_param", shipmentId != null ? shipmentId : "none"
                ));
            }
            
            return result;
        } catch (Exception e) {
            return List.of(Map.of(
                "error", true,
                "message", e.getMessage(),
                "type", e.getClass().getSimpleName(),
                "credit_card_param", creditCard != null ? creditCard : "none",
                "shipment_id_param", shipmentId != null ? shipmentId : "none"
            ));
        }
    }
    
    /**
     * Validates input parameters to prevent SQL injection
     * This method can be registered as a security control in Contrast
     * 
     * @param input The input string to validate
     * @return True if the input is valid, false otherwise
     */
    private boolean validateSqlInput(String input) {
        // This method is provided as a placeholder for a security control
        // It's not used in the current implementation as we're using parameterized queries instead
        return input != null && !input.contains("'") && !input.contains(";");
    }
}