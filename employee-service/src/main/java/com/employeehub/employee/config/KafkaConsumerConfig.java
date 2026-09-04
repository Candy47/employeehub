package com.employeehub.employee.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Retry + Dead Letter Topic handling for the UserRegistered consumer.
 * Failed events are retried a few times, then routed to <topic>.DLT.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public NewTopic userRegistrationDltTopic() {
        return TopicBuilder
                .name("employeehub.USER.DLT")
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
}

