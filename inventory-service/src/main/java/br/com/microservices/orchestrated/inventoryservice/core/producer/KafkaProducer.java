package br.com.microservices.orchestrated.inventoryservice.core.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${spring.kafka.topic.orchestrator}")
    private String orchestratorTopic;
    
    public void sendEvent(final String payload) {
        try {
            log.info("Sending event to topic {} with data {}", this.orchestratorTopic, payload);
            this.kafkaTemplate.send(this.orchestratorTopic, payload);
        } catch (final Exception e) {
            log.error("Error trying to send data to topic {} with data {}", this.orchestratorTopic, payload, e);
        }
    }
    
}

