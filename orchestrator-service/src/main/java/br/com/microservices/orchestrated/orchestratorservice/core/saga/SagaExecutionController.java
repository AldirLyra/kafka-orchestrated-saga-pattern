package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.EVENT_SOURCE_INDEX;
import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.SAGA_HANDLER;
import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.SAGA_STATUS_INDEX;
import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.TOPIC_INDEX;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.orchestratorservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class SagaExecutionController {
    
    private static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID %s | EVENT ID %s";
    
    public ETopics getNextTopic(final Event event) {
        if (isEmpty(event.getSource()) || isEmpty(event.getStatus())) {
            throw new ValidationException("Source and status must be informed.");
        }
        
        final var topic = this.findTopicBySourceAndStatus(event);
        this.logCurrentSaga(event, topic);
        
        return topic;
    }
    
    private ETopics findTopicBySourceAndStatus(final Event event) {
        return (ETopics) (Arrays.stream(SAGA_HANDLER)
                                .filter(row -> this.isEventSourceAndStatusValid(event, row))
                                .map(i -> i[TOPIC_INDEX])
                                .findFirst()
                                .orElseThrow(() -> new ValidationException("Topic not found!")));
    }
    
    private boolean isEventSourceAndStatusValid(
            final Event event,
            final Object[] row) {
        
        final var source = row[EVENT_SOURCE_INDEX];
        final var status = row[SAGA_STATUS_INDEX];
        
        return source.equals(event.getSource()) && status.equals(event.getStatus());
    }
    
    private void logCurrentSaga(final Event event, final ETopics topic) {
        final var sagaId = this.createSagaId(event);
        final var source = event.getSource();
        
        switch (event.getStatus()) {
            case SUCCESS -> log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {}", source, topic, sagaId);
            case ROLLBACK_PENDING -> log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {}", source, topic, sagaId);
            case FAIL -> log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {}", source, topic, sagaId);
        }
    }
    
    private String createSagaId(final Event event) {
        return String.format(SAGA_LOG_ID, event.getPayload().getId(), event.getTransactionId(), event.getId());
    }
    
}
