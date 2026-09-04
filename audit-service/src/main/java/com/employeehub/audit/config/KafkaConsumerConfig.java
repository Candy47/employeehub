package com.employeehub.audit.config;

import com.employeehub.audit.event.EmployeeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic userRegistrationDltTopic() {
        return TopicBuilder
                .name("employeehub.USER.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic employeeDltTopic() {
        return TopicBuilder
                .name("employeehub.EMPLOYEE.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> {

                            log.error("""
                                    
                                    ============================================================
                                     RETRIES EXHAUSTED - PUBLISHING TO DEAD LETTER TOPIC
                                    ============================================================
                                    Original Topic : {}
                                    Dead Letter    : {}.DLT
                                    Event Key      : {}
                                    Partition      : {}
                                    Offset         : {}
                                    Cause          : {}
                                    ============================================================
                                    """,
                                    record.topic(),
                                    record.topic(),
                                    record.key(),
                                    record.partition(),
                                    record.offset(),
                                    exception.getCause() != null
                                            ? exception.getCause().getMessage()
                                            : exception.getMessage()
                            );

                            return new TopicPartition(
                                    record.topic() + ".DLT",
                                    record.partition()
                            );
                        }
                );

        /*
         * Initial attempt + 2 retries = 3 total attempts
         * Retry interval = 2 seconds
         */
        FixedBackOff fixedBackOff = new FixedBackOff(
                2000L,
                2L
        );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, fixedBackOff);

        errorHandler.setRetryListeners(
                (record, exception, deliveryAttempt) ->
                        log.warn("""
                                
                                ============================================================
                                 EVENT PROCESSING FAILED
                                ============================================================
                                Topic           : {}
                                Event Key       : {}
                                Partition       : {}
                                Offset          : {}
                                Retry Attempt   : {}/3
                                Reason          : {}
                                Next Action     : Retrying in 2 seconds...
                                ============================================================
                                """,
                                record.topic(),
                                record.key(),
                                record.partition(),
                                record.offset(),
                                deliveryAttempt,
                                exception.getCause() != null
                                        ? exception.getCause().getMessage()
                                        : exception.getMessage()
                        )
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object>
    kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }

    /**
     * Builds the employee-event consumer factory. NOTE: this is intentionally a
     * plain helper method, NOT a @Bean. Exposing a ConsumerFactory bean would
     * make Spring Boot back off from auto-configuring the default
     * ConsumerFactory (@ConditionalOnMissingBean), breaking the default
     * kafkaListenerContainerFactory used by the UserRegistered listener.
     */
    private ConsumerFactory<String, EmployeeEvent> employeeConsumerFactory() {

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "audit-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<EmployeeEvent> valueDeserializer =
                new JsonDeserializer<>(EmployeeEvent.class);
        valueDeserializer.addTrustedPackages("com.employeehub.audit.event");
        valueDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmployeeEvent>
    employeeKafkaListenerContainerFactory(DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, EmployeeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(employeeConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}