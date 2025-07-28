-- Create the credit_cards database if it doesn't exist
CREATE DATABASE IF NOT EXISTS credit_cards;

-- Grant all privileges on credit_cards database to cargocats user
GRANT ALL PRIVILEGES ON credit_cards.* TO 'cargocats'@'%';

-- Create the credit card table in the credit_cards database
USE credit_cards;
CREATE TABLE IF NOT EXISTS credit_card (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  card_number VARCHAR(255) NOT NULL,
  shipment_id BIGINT NOT NULL
);

-- Return to the default database
USE db;