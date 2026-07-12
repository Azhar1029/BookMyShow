package com.cfs.BMS2.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Spring Boot auto-configures the Kafka listener container factory, and will pick up
 * any CommonErrorHandler bean found in the context (like this one) and wire it in
 * automatically - no need to hand-build the whole ConcurrentKafkaListenerContainerFactory.
 *
 * Without this bean, a listener-method exception (e.g. Gmail SMTP down/misconfigured in
 * NotificationConsumer -> EmailService) falls back to Spring Kafka's default retry
 * behavior: a handful of retries, then the message is silently dropped with only a
 * generic log line buried in the framework's own logging. In practice that means a
 * customer's booking confirmation email can vanish with no clear trace of why.
 *
 * This gives us the same "retry a few times, then give up" behavior, but with an
 * explicit, greppable ERROR log naming the topic/key/exception when it finally gives up,
 * so a failed notification is visible instead of silently swallowed.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(
                this::logFailedRecord,
                new FixedBackOff(2000L, 3) // retry 3 times, 2s apart, then give up
        );
    }

    private void logFailedRecord(ConsumerRecord<?, ?> record, Exception exception) {
        log.error(
                "Kafka message on topic '{}' (partition={}, offset={}, key={}) failed after retries and was skipped: {}",
                record.topic(), record.partition(), record.offset(), record.key(), exception.getMessage(), exception
        );
    }
}
