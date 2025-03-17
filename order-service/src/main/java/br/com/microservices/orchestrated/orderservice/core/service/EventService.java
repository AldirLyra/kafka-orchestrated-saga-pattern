package br.com.microservices.orchestrated.orderservice.core.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilters;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    
    private final EventRepository eventRepository;
    
    public void notifyEnding(final Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        
        this.save(event);
        
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }
    
    public List<Event> findAll() {
        return this.eventRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Event findByFilters(final EventFilters filters) {
        this.validateEmptyFilters(filters);
        
        if (!filters.getOrderId().isEmpty()) {
            return this.eventRepository
                    .findTop1ByOrderIdOrderByCreatedAtDesc(filters.getOrderId())
                    .orElseThrow(() -> new ValidationException("Event not found by OrderID"));
            
        } else {
            return this.eventRepository
                    .findTop1ByTransactionIdOrderByCreatedAtDesc(filters.getTransactionId())
                    .orElseThrow(() -> new ValidationException("Event not found by TransactionID"));
        }
    }
    
    private void validateEmptyFilters(final EventFilters filters) {
        if (filters.getOrderId().isEmpty() && filters.getTransactionId().isEmpty()) {
            throw new ValidationException("OrderID or TransactionID must be informed");
        }
    }
    
    public Event save(final Event event) {
        return this.eventRepository.save(event);
    }
    
}
