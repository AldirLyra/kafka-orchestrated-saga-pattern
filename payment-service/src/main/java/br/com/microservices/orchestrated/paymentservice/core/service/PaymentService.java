package br.com.microservices.orchestrated.paymentservice.core.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.paymentservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.History;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProduct;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    
    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    
    private static final Double REDUCE_SUM_VALUE = 0.0;
    
    private static final Double MIN_AMOUNT_VALUE = 0.1;
    
    private final JsonUtil jsonUtil;
    
    private final KafkaProducer producer;
    
    private final PaymentRepository paymentRepository;
    
    public void realizePayment(final Event event) {
        try {
            this.checkCurrentValidation(event);
            this.createPendingPayment(event);
            
            final var payment = this.findByOrderIdAndTransactionId(event);
            this.validateAmount(payment.getTotalAmount());
            this.changePaymentToSuccess(payment);
            this.handleSuccess(event);
            
        } catch (final Exception e) {
            log.error("Error trying to make payment: ", e);
            this.handleFailCurrentNotExecuted(event, e.getMessage());
        }
        
        this.producer.sendEvent(this.jsonUtil.toJson(event));
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
    
    private void checkCurrentValidation(final Event event) {
        if (this.paymentRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation.");
        }
    }
    
    private void createPendingPayment(final Event event) {
        final var totalAmount = this.calculateAmount(event);
        final var totalItems = this.calculateTotalItems(event);
        
        final var payment = Payment
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
        
        this.save(payment);
        this.setEventAmountItems(event, payment);
    }
    
    private double calculateAmount(final Event event) {
        return event.getPayload()
                    .getProducts()
                    .stream()
                    .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
                    .reduce(REDUCE_SUM_VALUE, Double::sum);
    }
    
    private int calculateTotalItems(final Event event) {
        return event.getPayload()
                    .getProducts()
                    .stream()
                    .map(OrderProduct::getQuantity)
                    .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }
    
    private void setEventAmountItems(final Event event, final Payment payment) {
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }
    
    private void validateAmount(final double amount) {
        if (amount < MIN_AMOUNT_VALUE) {
            throw new ValidationException("The minimum amount available is ".concat(MIN_AMOUNT_VALUE.toString()));
        }
    }
    
    private void handleSuccess(final Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        this.addHistory(event, "Payment realized successfully!");
    }
    
    private void handleFailCurrentNotExecuted(final Event event, final String message) {
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        this.addHistory(event, "Fail to realize payment: " + message);
    }
    
    public void realizeRefund(final Event event) {
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        
        try {
            this.changePaymentsStatusToRefund(event);
            this.addHistory(event, "Rollback executed for payment!");
            
        } catch (final Exception e) {
            this.addHistory(event, "Rollback not executed for payment: ".concat(e.getMessage()));
        }
        
        this.producer.sendEvent(this.jsonUtil.toJson(event));
    }
    
    private void changePaymentsStatusToRefund(final Event event) {
        final var payment = this.findByOrderIdAndTransactionId(event);
        payment.setStatus(EPaymentStatus.REFUND);
        this.setEventAmountItems(event, payment);
        this.save(payment);
    }
    
    private void changePaymentToSuccess(final Payment payment) {
        payment.setStatus(EPaymentStatus.SUCCESS);
        this.save(payment);
    }
    
    private Payment findByOrderIdAndTransactionId(final Event event) {
        return this.paymentRepository
                .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .orElseThrow(() -> new ValidationException("Payment not found by OrderID and TransactionID."));
    }
    
    private void save(final Payment payment) {
        this.paymentRepository.save(payment);
    }
    
}
