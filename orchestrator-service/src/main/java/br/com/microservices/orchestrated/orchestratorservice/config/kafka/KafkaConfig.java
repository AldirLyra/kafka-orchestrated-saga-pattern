package br.com.microservices.orchestrated.orchestratorservice.config.kafka;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.BASE_ORCHESTRATOR;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.FINISH_FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.FINISH_SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.INVENTORY_FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.INVENTORY_SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.NOTIFY_ENDING;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.PAYMENT_FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.PAYMENT_SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.PRODUCT_VALIDATION_FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.PRODUCT_VALIDATION_SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.START_SAGA;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.RequiredArgsConstructor;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    
    private static final Integer PARTITION_COUNT = 1;
    
    private static final Integer REPLICA_COUNT = 1;
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(this.consumerProps());
    }
    
    private Map<String, Object> consumerProps() {
        final Map<String, Object> props = new HashMap<>();
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.autoOffsetReset);
        
        return props;
    }
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(this.producerProps());
    }
    
    private Map<String, Object> producerProps() {
        final Map<String, Object> props = new HashMap<>();
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        return props;
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(final ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    private NewTopic buildTopic(final String name) {
        return TopicBuilder
                .name(name)
                .replicas(REPLICA_COUNT)
                .partitions(PARTITION_COUNT)
                .build();
    }
    
    @Bean
    public NewTopic startSagaTopic() {
        return this.buildTopic(START_SAGA.getTopic());
    }
    
    @Bean
    public NewTopic orchestratorTopic() {
        return this.buildTopic(BASE_ORCHESTRATOR.getTopic());
    }
    
    @Bean
    public NewTopic finishSuccessTopic() {
        return this.buildTopic(FINISH_SUCCESS.getTopic());
    }
    
    @Bean
    public NewTopic finishFailTopic() {
        return this.buildTopic(FINISH_FAIL.getTopic());
    }
    
    @Bean
    public NewTopic productValidationSuccessTopic() {
        return this.buildTopic(PRODUCT_VALIDATION_SUCCESS.getTopic());
    }
    
    @Bean
    public NewTopic productValidationFailTopic() {
        return this.buildTopic(PRODUCT_VALIDATION_FAIL.getTopic());
    }
    
    @Bean
    public NewTopic paymentSuccessTopic() {
        return this.buildTopic(PAYMENT_SUCCESS.getTopic());
    }
    
    @Bean
    public NewTopic paymentFailTopic() {
        return this.buildTopic(PAYMENT_FAIL.getTopic());
    }
    
    @Bean
    public NewTopic inventorySuccessTopic() {
        return this.buildTopic(INVENTORY_SUCCESS.getTopic());
    }
    
    @Bean
    public NewTopic inventoryValidationFailTopic() {
        return this.buildTopic(INVENTORY_FAIL.getTopic());
    }
    
    @Bean
    public NewTopic notifyEndingTopic() {
        return this.buildTopic(NOTIFY_ENDING.getTopic());
    }
    
}
