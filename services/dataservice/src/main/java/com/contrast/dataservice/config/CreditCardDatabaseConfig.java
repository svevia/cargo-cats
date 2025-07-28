package com.contrast.dataservice.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CreditCardDatabaseConfig {

    // Credit Cards DataSource Properties
    @Bean(name = "creditCardsProperties")
    @ConfigurationProperties("spring.datasource.creditcards")
    public DataSourceProperties creditCardsProperties() {
        return new DataSourceProperties();
    }
    
    // Credit Cards DataSource
    @Bean(name = "creditCardsDataSource")
    public DataSource creditCardsDataSource(@Qualifier("creditCardsProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .build();
    }

    // JdbcTemplate for credit cards database
    @Bean(name = "creditCardsJdbcTemplate")
    public JdbcTemplate creditCardsJdbcTemplate(@Qualifier("creditCardsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}