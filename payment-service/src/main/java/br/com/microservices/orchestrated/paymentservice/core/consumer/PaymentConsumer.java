package br.com.microservices.orchestrated.paymentservice.core.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.paymentservice.core.service.PaymentService;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class PaymentConsumer {
    
    private JsonUtil jsonUtil;
    
    private final PaymentService paymentService;
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.payment-success}"
    )
    public void consumeSuccessEvent(final String payload) {
        log.info("Receiving success event {} from payment-success topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.paymentService.realizePayment(event);
    }
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.payment-fail}"
    )
    public void consumeFailEvent(final String payload) {
        log.info("Receiving rollback event {} from payment-fail topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.paymentService.realizeRefund(event);
    }
    
}
