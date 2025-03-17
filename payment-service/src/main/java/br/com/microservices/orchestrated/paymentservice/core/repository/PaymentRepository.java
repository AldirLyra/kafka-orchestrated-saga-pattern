package br.com.microservices.orchestrated.paymentservice.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.microservices.orchestrated.paymentservice.core.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    
    Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId);
    
}
