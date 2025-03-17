package br.com.microservices.orchestrated.inventoryservice.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;

public interface OrderInventoryRepository extends JpaRepository<OrderInventory, Integer> {
    
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    
    List<OrderInventory> findByOrderIdAndTransactionId(String orderId, String transactionId);
    
}
