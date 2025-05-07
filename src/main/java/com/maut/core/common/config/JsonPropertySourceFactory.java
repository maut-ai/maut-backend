package com.maut.core.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Custom PropertySourceFactory for loading properties from JSON files.
 * This allows Spring to read configuration from JSON files using @PropertySource.
 * Used across all modules for consistent configuration handling.
 */
public class JsonPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Map<String, Object> readValue = new ObjectMapper()
                .readValue(resource.getInputStream(), Map.class);
        
        String sourceName = name != null ? name : resource.getResource().getFilename();
        return new MapPropertySource(sourceName, readValue);
    }
}
