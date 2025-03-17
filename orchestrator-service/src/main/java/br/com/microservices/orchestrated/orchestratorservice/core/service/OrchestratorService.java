package br.com.microservices.orchestrated.orchestratorservice.core.service;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.ORCHESTRATOR;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.SUCCESS;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.History;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import br.com.microservices.orchestrated.orchestratorservice.core.producer.SagaOrchestatorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class OrchestratorService {
    
    private final JsonUtil jsonUtil;
    
    private final SagaOrchestatorProducer producer;
    
    private final SagaExecutionController sagaExecutionController;
    
    public void startSaga(final Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        final var topic = this.getTopic(event);
        log.info("SAGA STARTED!");
        this.addHistory(event, "Saga started!");
        this.sendToProducerWithTopic(event, topic);
    }
    
    public void finishSagaSuccess(final Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}!", event.getId());
        this.addHistory(event, "Saga finished successfully!");
        this.notifyFinishedSaga(event);
    }
    
    public void finishSagaFail(final Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(FAIL);
        log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}!", event.getId());
        this.addHistory(event, "Saga finished with errors!");
        this.notifyFinishedSaga(event);
    }
    
    public void continueSaga(final Event event) {
        final var topic = this.getTopic(event);
        log.info("SAGA CONTINUING FOR EVENT {}", event.getId());
        this.sendToProducerWithTopic(event, topic);
    }
    
    private ETopics getTopic(final Event event) {
        return this.sagaExecutionController.getNextTopic(event);
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
    
    private void sendToProducerWithTopic(final Event event, final ETopics topic) {
        this.producer.sendEvent(this.jsonUtil.toJson(event), topic.getTopic());
    }
    
    private void notifyFinishedSaga(final Event event) {
        this.producer.sendEvent(this.jsonUtil.toJson(event), ETopics.NOTIFY_ENDING.getTopic());
    }
    
}
