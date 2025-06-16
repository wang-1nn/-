package org.example.backend.config;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class RagConfig {
    @Bean
    @Value("classpath:text-source.txt")
    public Resource textSource(Resource resource) {      // ← org.springframework.core.io.Resource
        return resource;
    }
}

