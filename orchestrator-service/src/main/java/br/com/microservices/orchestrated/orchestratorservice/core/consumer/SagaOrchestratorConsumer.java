package br.com.microservices.orchestrated.orchestratorservice.core.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.orchestratorservice.core.service.OrchestratorService;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class SagaOrchestratorConsumer {
    
    private final JsonUtil jsonUtil;
    
    private final OrchestratorService orchestratorService;
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.start-saga}"
    )
    public void consumeStartSagaEvent(final String payload) {
        log.info("Receiving event {} from start-saga topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.orchestratorService.startSaga(event);
    }
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.orchestrator}"
    )
    public void consumeOrchestratorEvent(final String payload) {
        log.info("Receiving event {} from orchestrator topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.orchestratorService.continueSaga(event);
    }
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-success}"
    )
    public void consumeFinishSuccessEvent(final String payload) {
        log.info("Receiving event {} from finish-success topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.orchestratorService.finishSagaSuccess(event);
    }
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-fail}"
    )
    public void consumeFinishFailEvent(final String payload) {
        log.info("Receiving event {} from finish-fail topic", payload);
        final var event = this.jsonUtil.toEvent(payload);
        this.orchestratorService.finishSagaFail(event);
    }
    
}
