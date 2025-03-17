package br.com.microservices.orchestrated.orderservice.core.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.document.Order;
import br.com.microservices.orchestrated.orderservice.core.dto.OrderRequest;
import br.com.microservices.orchestrated.orderservice.core.producer.SagaProducer;
import br.com.microservices.orchestrated.orderservice.core.repository.OrderRepository;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderService {
    
    private static final String TRANSACTION_ID_FORMAT = "%s_%s";
    
    private final OrderRepository orderRepository;
    
    private final JsonUtil jsonUtil;
    
    private final SagaProducer producer;
    
    private final EventService eventService;
    
    public Order createOrder(final OrderRequest orderRequest) {
        final Order order = Order
                .builder()
                .products(orderRequest.getProducts())
                .createdAt(LocalDateTime.now())
                .transactionId(String.format(TRANSACTION_ID_FORMAT, Instant.now().toEpochMilli(), UUID.randomUUID()))
                .build();
        
        this.orderRepository.save(order);
        this.producer.sendEvent(this.jsonUtil.toJson(this.createPayload(order)));
        
        return order;
    }
    
    private Event createPayload(final Order order) {
        final Event event = Event
                .builder()
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();
        
        this.eventService.save(event);
        
        return event;
    }
    
}
