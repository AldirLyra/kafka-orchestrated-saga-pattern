package br.com.microservices.orchestrated.inventoryservice.core.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.inventoryservice.core.service.InventoryService;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class InventoryConsumer {
    
    private JsonUtil jsonUtil;
    
    private final InventoryService inventoryService;
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-success}"
    )
    public void consumeSuccessEvent(final String payload) {
        log.info("Receiving success event {} from inventory-success topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.inventoryService.updateInventory(event);
    }
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-fail}"
    )
    public void consumeFailEvent(final String payload) {
        log.info("Receiving rollback event {} from inventory-fail topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.inventoryService.rollbackInventory(event);
    }
    
}
