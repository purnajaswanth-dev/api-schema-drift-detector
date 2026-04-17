package com.ApiDrift.detector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class SchemaExtractorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> extractSchema(JsonNode rootNode, String prefix) {
        Map<String, String> schema = new HashMap<>();
        
        if (rootNode == null || rootNode.isNull()) {
            schema.put(prefix.isEmpty() ? "root" : prefix, "Null");
            return schema;
        }

        if (rootNode.isObject()) {
            if (!prefix.isEmpty()) {
                schema.put(prefix, "Object");
            }
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                schema.putAll(extractSchema(entry.getValue(), newPrefix));
            }
        } else if (rootNode.isArray()) {
            schema.put(prefix.isEmpty() ? "root" : prefix, "Array");
            if (rootNode.size() > 0) {
                String newPrefix = prefix.isEmpty() ? "[0]" : prefix + "[0]";
                schema.putAll(extractSchema(rootNode.get(0), newPrefix));
            }
        } else if (rootNode.isTextual()) {
            schema.put(prefix.isEmpty() ? "root" : prefix, "String");
        } else if (rootNode.isInt()) {
            schema.put(prefix.isEmpty() ? "root" : prefix, "Integer");
        } else if (rootNode.isLong()) {
            schema.put(prefix.isEmpty() ? "root" : prefix, "Long");
        } else if (rootNode.isDouble() || rootNode.isFloat()) {
            schema.put(prefix.isEmpty() ? "root" : prefix, "Double");
        } else if (rootNode.isBoolean()) {
            schema.put(prefix.isEmpty() ? "root" : prefix, "Boolean");
        } else {
            schema.put(prefix.isEmpty() ? "root" : prefix, "Unknown");
        }

        return schema;
    }

    public String schemaToJson(Map<String, String> schema) {
        try {
            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize schema to JSON", e);
        }
    }

    public Map<String, String> jsonToSchema(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to schema", e);
        }
    }
}
