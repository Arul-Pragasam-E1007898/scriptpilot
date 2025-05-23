package com.freshworks.ex.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON serialization and deserialization operations.
 * This class provides methods to convert between JSON strings and Java objects
 * using Jackson's ObjectMapper.
 */
public class Serializer {
    /** Jackson ObjectMapper instance for JSON processing */
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new Serializer instance.
     * Initializes a new ObjectMapper for JSON processing.
     */
    public Serializer() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Parses a JSON string into a JsonNode object.
     * 
     * @param json The JSON string to parse
     * @return JsonNode representing the parsed JSON structure
     * @throws JsonProcessingException if the JSON string is malformed or cannot be parsed
     */
    public JsonNode parse(String json) throws JsonProcessingException {
        return objectMapper.readTree(json);
    }

    /**
     * Serializes a Java object into a JSON string.
     * 
     * @param value The object to serialize
     * @return JSON string representation of the object
     * @throws JsonProcessingException if the object cannot be serialized to JSON
     */
    public String serialize(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}
