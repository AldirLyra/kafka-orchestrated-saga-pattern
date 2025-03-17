package br.com.microservices.orchestrated.orchestratorservice.core.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class SagaOrchestatorProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public void sendEvent(final String payload, final String topic) {
        try {
            log.info("Sending event to topic {} with data {}", topic, payload);
            this.kafkaTemplate.send(topic, payload);
        } catch (final Exception e) {
            log.error("Error trying to send data to topic {} with data {}", topic, payload, e);
        }
    }
    
}
