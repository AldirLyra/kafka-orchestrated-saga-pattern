package br.com.microservices.orchestrated.productvalidationservice.core.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.productvalidationservice.core.service.ProductValidationService;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class ProductValidationConsumer {
    
    private final ProductValidationService productValidationService;
    
    private JsonUtil jsonUtil;
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-success}"
    )
    public void consumeSuccessEvent(final String payload) {
        log.info("Receiving success event {} from product-validation-success topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.productValidationService.validateExistingProducts(event);
    }
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-fail}"
    )
    public void consumeFailEvent(final String payload) {
        log.info("Receiving rollback event {} from product-validation-fail topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.productValidationService.rollbackEvent(event);
    }
    
}
