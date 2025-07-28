package com.contrast.dataservice.entity;

/**
 * Simple POJO for credit card data - not managed by JPA
 * since we're using direct JDBC access for the credit cards database
 */
public class CreditCard {

    private Long id;
    private String cardNumber;
    private Long shipmentId;

    public CreditCard() {
    }

    public CreditCard(String cardNumber, Long shipmentId) {
        this.cardNumber = cardNumber;
        this.shipmentId = shipmentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }
}