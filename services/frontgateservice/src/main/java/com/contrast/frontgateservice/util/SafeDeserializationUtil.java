package com.contrast.frontgateservice.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for safe deserialization operations
 */
public class SafeDeserializationUtil {
    
    private static final Logger logger = LogManager.getLogger(SafeDeserializationUtil.class);
    
    /**
     * Safely deserialize an object from an input stream with a whitelist filter
     * 
     * @param inputStream The input stream to deserialize from
     * @param allowedClasses Array of allowed classes for deserialization
     * @return The deserialized object
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If the class of a serialized object cannot be found
     */
    public static Object safeDeserialize(InputStream inputStream, Class<?>... allowedClasses) 
            throws IOException, ClassNotFoundException {
        
        // Create a filter that only allows specific classes
        ObjectInputFilter filter = createWhitelistFilter(allowedClasses);
        
        try (ObjectInputStream ois = new FilteredObjectInputStream(inputStream, filter)) {
            return ois.readObject();
        }
    }
    
    /**
     * Creates a whitelist filter for object deserialization
     * 
     * @param allowedClasses Array of allowed classes
     * @return An ObjectInputFilter that only allows the specified classes
     */
    private static ObjectInputFilter createWhitelistFilter(Class<?>... allowedClasses) {
        return filterInfo -> {
            Class<?> clazz = filterInfo.serialClass();
            
            // Always allow primitive types and their wrappers, and String
            if (clazz == null || 
                clazz.isPrimitive() || 
                clazz == String.class ||
                Number.class.isAssignableFrom(clazz) ||
                clazz == Boolean.class ||
                clazz == Character.class) {
                return ObjectInputFilter.Status.ALLOWED;
            }
            
            // Allow standard collection classes
            if (clazz == ArrayList.class || 
                clazz == HashMap.class ||
                List.class.isAssignableFrom(clazz) ||
                Map.class.isAssignableFrom(clazz)) {
                return ObjectInputFilter.Status.ALLOWED;
            }
            
            // Check against the whitelist
            for (Class<?> allowedClass : allowedClasses) {
                if (allowedClass.isAssignableFrom(clazz)) {
                    return ObjectInputFilter.Status.ALLOWED;
                }
            }
            
            logger.warn("Blocked deserialization of class: {}", clazz.getName());
            return ObjectInputFilter.Status.REJECTED;
        };
    }
    
    /**
     * Custom ObjectInputStream that applies a filter
     */
    private static class FilteredObjectInputStream extends ObjectInputStream {
        private final ObjectInputFilter filter;
        
        public FilteredObjectInputStream(InputStream in, ObjectInputFilter filter) throws IOException {
            super(in);
            this.filter = filter;
            this.setObjectInputFilter(filter);
        }
    }
}