package org.apache.camel.example.springboot.numbers.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public record CommandMessage(String command, Map<String, String> params) {

    @JsonIgnore
    private static final AtomicInteger errorCount = new AtomicInteger(0);

    @JsonIgnore
    private static final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    @Override
    public String toString() {
        String result;
        try {
            result = objectMapper.writeValueAsString(this);
        } catch (Exception ex) {
            result = Map.of("errorCount", String.valueOf(errorCount.incrementAndGet()),
                            "errorMessage", ex.getMessage()).entrySet().stream()
                    .map(e -> String.format("""
                            "%s":"%s"
                            """, e.getKey(), e.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        return result;
    }
}
