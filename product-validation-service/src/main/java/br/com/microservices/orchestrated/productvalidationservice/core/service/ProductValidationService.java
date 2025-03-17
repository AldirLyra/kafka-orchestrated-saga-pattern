package br.com.microservices.orchestrated.productvalidationservice.core.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.productvalidationservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.Event;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.History;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.OrderProduct;
import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.productvalidationservice.core.model.Validation;
import br.com.microservices.orchestrated.productvalidationservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ProductRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ValidationRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ProductValidationService {
    
    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";
    
    private final JsonUtil jsonUtil;
    
    private final KafkaProducer producer;
    
    private final ProductRepository productRepository;
    
    private final ValidationRepository validationRepository;
    
    public void validateExistingProducts(final Event event) {
        try {
            this.checkCurrentValidation(event);
            this.createValidation(event, true);
            this.handleSuccess(event);
            
        } catch (final Exception e) {
            log.error("Error trying to validate products: ", e);
            this.handleFailCurrentNotExecuted(event, e.getMessage());
        }
        
        this.producer.sendEvent(this.jsonUtil.toJson(event));
    }
    
    private void checkCurrentValidation(final Event event) {
        this.validateProductsInformed(event);
        
        if (this.validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation.");
        }
        
        event.getPayload().getProducts().forEach(product -> {
            this.validateProductInformed(product);
            this.validateExistingProduct(product.getProduct().getCode());
        });
    }
    
    private void validateProductInformed(final OrderProduct product) {
        if (isEmpty(product.getProduct()) || isEmpty(product.getProduct().getCode())) {
            throw new ValidationException("Product must be informed!");
        }
    }
    
    private void validateExistingProduct(final String code) {
        if (!this.productRepository.existsByCode(code)) {
            throw new ValidationException("Product does not exists in database!");
        }
    }
    
    private void validateProductsInformed(final Event event) {
        if (isEmpty(event.getPayload()) || isEmpty(event.getPayload().getProducts())) {
            throw new ValidationException("Products list is empty!");
        }
        
        if (isEmpty(event.getPayload().getId()) || isEmpty(event.getPayload().getTransactionId())) {
            throw new ValidationException("OrderID and TransactionID must be informed!");
        }
    }
    
    private void createValidation(final Event event, final boolean success) {
        final var validation = Validation
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .success(success)
                .build();
        
        this.validationRepository.save(validation);
    }
    
    private void handleSuccess(final Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        this.addHistory(event, "Products are validated successfully!");
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
    
    private void handleFailCurrentNotExecuted(final Event event, final String message) {
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        this.addHistory(event, "Fail to validate products: " + message);
    }
    
    public void rollbackEvent(final Event event) {
        this.changeValidationToFail(event);
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        this.addHistory(event, "Rollback executed on product validation!");
        this.producer.sendEvent(this.jsonUtil.toJson(event));
    }
    
    private void changeValidationToFail(final Event event) {
        this.validationRepository
                .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .ifPresentOrElse(validation -> {
                    validation.setSuccess(false);
                    this.validationRepository.save(validation);
                }, () -> this.createValidation(event, false));
    }
    
}
