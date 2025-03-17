package br.com.microservices.orchestrated.orderservice.core.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.orderservice.core.service.EventService;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class EventConsumer {
    
    private final EventService eventService;
    
    private final JsonUtil jsonUtil;
    
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.notify-ending}"
    )
    public void consumeNotifyEndingTopic(final String payload) {
        log.info("Receiving ending notification event {} from notify-ending topic", payload);
        this.eventService.notifyEnding(this.jsonUtil.toEvent(payload));
    }
    
}
