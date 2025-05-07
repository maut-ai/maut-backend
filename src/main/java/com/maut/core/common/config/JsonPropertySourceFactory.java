package com.maut.core.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom PropertySourceFactory for loading properties from JSON files.
 * This allows Spring to read configuration from JSON files using @PropertySource.
 * Used across all modules for consistent configuration handling.
 */
public class JsonPropertySourceFactory implements PropertySourceFactory {

    private static final Logger log = LoggerFactory.getLogger(JsonPropertySourceFactory.class);

    @Override
    @NonNull
    public PropertySource<?> createPropertySource(@Nullable String name, @NonNull EncodedResource resource) throws IOException {
        Assert.notNull(resource, "EncodedResource must not be null");
        log.info("Attempting to load JSON property source. Initial name: '{}', Resource: '{}'", name, resource.getResource().getFilename());

        Map<String, Object> rawJsonMap;
        try {
            rawJsonMap = new ObjectMapper()
                    .readValue(resource.getInputStream(), new TypeReference<Map<String, Object>>() {});
            log.debug("Successfully read JSON from resource. Number of root properties: {}", rawJsonMap.size());
        } catch (IOException e) {
            log.error("Failed to read JSON from resource: {}", resource.getResource().getFilename(), e);
            throw e;
        }

        Map<String, Object> flattenedMap = new LinkedHashMap<>();
        flattenMap(null, rawJsonMap, flattenedMap);

        if (log.isTraceEnabled()) {
            flattenedMap.keySet().forEach(key -> log.trace("Loaded flattened property key: {} -> value: {}", key, flattenedMap.get(key)));
        }

        // Determine the source name. Ensure it's never null.
        String resolvedName = name;
        if (resolvedName == null) {
            String resourceFilename = resource.getResource().getFilename();
            if (resourceFilename != null) {
                resolvedName = resourceFilename;
            } else {
                // Fallback if both name and filename are null
                resolvedName = "applicationConfigJson"; // More specific fallback name
            }
        }

        // The logic above ensures resolvedName is not null. Assert for safety.
        Assert.notNull(resolvedName, "Property source name must not be null after resolution.");
        log.info("Resolved property source name: '{}' for resource: '{}'", resolvedName, resource.getResource().getFilename());

        return new MapPropertySource(resolvedName, flattenedMap);
    }

    private void flattenMap(String currentPath, Map<String, Object> source, Map<String, Object> target) {
        source.forEach((key, value) -> {
            String newPath = (currentPath == null || currentPath.isEmpty()) ? key : currentPath + "." + key;
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                flattenMap(newPath, mapValue, target);
            } else if (value instanceof java.util.List) {
                // Handle lists by creating indexed properties, e.g., listName[0], listName[1]
                // This might be needed if you have lists in your config that need to be accessed by index via @Value
                // For now, just logging a warning as the current properties don't seem to use indexed list access.
                // If you need this, you'd convert the list to string or map entries like listName[0]=value0
                log.warn("List found at path '{}'. Direct list to @Value injection is complex. Consider mapping to comma-separated string or individual properties.", newPath);
                target.put(newPath, value); // Store as is, or convert to String
            } else {
                target.put(newPath, value);
            }
        });
    }
}
