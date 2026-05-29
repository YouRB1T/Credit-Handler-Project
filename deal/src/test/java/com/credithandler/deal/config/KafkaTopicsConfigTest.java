package com.credithandler.deal.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("Тестирование конфигурации Kafka-топиков")

class KafkaTopicsConfigTest {

    @Test
    @DisplayName("Kafka-топики связываются через ConfigurationProperties")
    void kafkaTopicsAreBoundThroughConfigurationProperties() {
        KafkaTopicsConfig config = new KafkaTopicsConfig();

        new Binder(new MapConfigurationPropertySource(Map.of(
                "kafka.topic.finish-registration", "finish-registration-topic",
                "kafka.topic.create-documents", "create-documents-topic",
                "kafka.topic.send-documents", "send-documents-topic",
                "kafka.topic.send-ses", "send-ses-topic",
                "kafka.topic.credit-issued", "credit-issued-topic",
                "kafka.topic.statement-denied", "statement-denied-topic"
        ))).bind("kafka.topic", Bindable.ofInstance(config));

        assertThat(config.getFinishRegistration()).isEqualTo("finish-registration-topic");
        assertThat(config.getCreateDocuments()).isEqualTo("create-documents-topic");
        assertThat(config.getSendDocuments()).isEqualTo("send-documents-topic");
        assertThat(config.getSendSes()).isEqualTo("send-ses-topic");
        assertThat(config.getCreditIssued()).isEqualTo("credit-issued-topic");
        assertThat(config.getStatementDenied()).isEqualTo("statement-denied-topic");
    }

    @Test
    @DisplayName("KafkaEmailMessageProducer не использует внедрение через Value")
    void kafkaEmailMessageProducerDoesNotUseValueInjection() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/credithandler/deal/service/impl/KafkaEmailMessageProducer.java"
        ));

        assertThat(source).doesNotContain("@Value");
    }
}
