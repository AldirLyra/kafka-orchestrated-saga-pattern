package br.com.microservices.orchestrated.inventoryservice.core.utils;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JsonUtil {
    
    private final ObjectMapper objectMapper;
    
    public String toJson(final Object object) {
        try {
            return this.objectMapper.writeValueAsString(object);
        } catch (final Exception e) {
            return "";
        }
    }
    
    public Event toEvent(final String json) {
        try {
            return this.objectMapper.readValue(json, Event.class);
        } catch (final Exception e) {
            return null;
        }
    }
    
}
