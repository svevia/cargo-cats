package com.contrast.dataservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.contrast.dataservice.entity.Cat;
import com.contrast.dataservice.entity.User;
import com.contrast.dataservice.entity.Address;
import com.contrast.dataservice.entity.Shipment;

@Configuration
public class RestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Expose entity IDs in JSON responses
        config.exposeIdsFor(Cat.class, User.class, Address.class, Shipment.class);
        
        // Configure CORS for Spring Data REST
        cors.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
