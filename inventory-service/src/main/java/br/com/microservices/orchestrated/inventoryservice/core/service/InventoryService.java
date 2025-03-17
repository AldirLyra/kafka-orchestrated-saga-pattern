package br.com.microservices.orchestrated.inventoryservice.core.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.inventoryservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.dto.History;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Order;
import br.com.microservices.orchestrated.inventoryservice.core.dto.OrderProduct;
import br.com.microservices.orchestrated.inventoryservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.inventoryservice.core.model.Inventory;
import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;
import br.com.microservices.orchestrated.inventoryservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.inventoryservice.core.repository.InventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.repository.OrderInventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class InventoryService {
    
    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";
    
    private final JsonUtil jsonUtil;
    
    private final KafkaProducer producer;
    
    private final InventoryRepository inventoryRepository;
    
    private final OrderInventoryRepository orderInventoryRepository;
    
    public void updateInventory(final Event event) {
        try {
            this.checkCurrentValidation(event);
            this.createOrderInventory(event);
            this.updateInventory(event.getPayload());
            this.handleSuccess(event);
            
        } catch (final Exception e) {
            log.error("Error trying to update inventory: ", e);
            this.handleFailCurrentNotExecuted(event, e.getMessage());
        }
        
        this.producer.sendEvent(this.jsonUtil.toJson(event));
    }
    
    private void checkCurrentValidation(final Event event) {
        if (this.orderInventoryRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation.");
        }
    }
    
    private void createOrderInventory(final Event event) {
        event.getPayload().getProducts().forEach(product -> {
            final var inventory = this.findInventoryByProductCode(product.getProduct().getCode());
            final var orderInventory = this.createOrderInventory(event, product, inventory);
            this.orderInventoryRepository.save(orderInventory);
        });
        
    }
    
    private OrderInventory createOrderInventory(final Event event, final OrderProduct product, final Inventory inventory) {
        return OrderInventory.builder()
                             .inventory(inventory)
                             .oldQuantity(inventory.getAvailable())
                             .orderQuantity(product.getQuantity())
                             .newQuantity(inventory.getAvailable() - product.getQuantity())
                             .orderId(event.getPayload().getId())
                             .transactionId(event.getTransactionId())
                             .build();
    }
    
    private void updateInventory(final Order order) {
        order.getProducts().forEach(product -> {
            final var inventory = this.findInventoryByProductCode(product.getProduct().getCode());
            this.checkInventory(inventory.getAvailable(), product.getQuantity());
            inventory.setAvailable(inventory.getAvailable() - product.getQuantity());
            this.inventoryRepository.save(inventory);
        });
    }
    
    private void checkInventory(final Integer available, final Integer orderQuantity) {
        if (orderQuantity > available) {
            throw new ValidationException("Product is out of stock!");
        }
    }
    
    private void handleSuccess(final Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        this.addHistory(event, "Inventory updated successfully!");
    }
    
    private void handleFailCurrentNotExecuted(final Event event, final String message) {
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        this.addHistory(event, "Fail to update inventory: " + message);
    }
    
    public void rollbackInventory(final Event event) {
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        
        try {
            this.returnInventoryToPreviousValues(event);
            this.addHistory(event, "Rollback executed for inventory!");
            
        } catch (final Exception e) {
            this.addHistory(event, "Rollback not executed for inventory: ".concat(e.getMessage()));
        }
        
        this.producer.sendEvent(this.jsonUtil.toJson(event));
    }
    
    private void returnInventoryToPreviousValues(final Event event) {
        this.orderInventoryRepository
                .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .forEach(orderInventory -> {
                    final var inventory = orderInventory.getInventory();
                    inventory.setAvailable(orderInventory.getOldQuantity());
                    this.inventoryRepository.save(inventory);
                    log.info("Restored inventory for order {} from {} to {}",
                             event.getPayload().getId(),
                             orderInventory.getNewQuantity(),
                             orderInventory.getOldQuantity());
                });
    }
    
    private void addHistory(final Event event, final String message) {
        final var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        
        event.addHistory(history);
    }
    
    private Inventory findInventoryByProductCode(final String productCode) {
        return this.inventoryRepository.findByProductCode(productCode)
                                       .orElseThrow(() -> new ValidationException("Inventory not found by informed product."));
    }
    
}
