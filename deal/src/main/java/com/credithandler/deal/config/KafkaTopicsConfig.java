package com.credithandler.deal.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka.topic")
public class KafkaTopicsConfig {

    private String finishRegistration;

    private String createDocuments;

    private String sendDocuments;

    private String sendSes;

    private String creditIssued;

    private String statementDenied;

    @Bean
    public NewTopic finishRegistrationTopic() {
        return TopicBuilder.name(finishRegistration).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic createDocumentsTopic() {
        return TopicBuilder.name(createDocuments).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sendDocumentsTopic() {
        return TopicBuilder.name(sendDocuments).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sendSesTopic() {
        return TopicBuilder.name(sendSes).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic creditIssuedTopic() {
        return TopicBuilder.name(creditIssued).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic statementDeniedTopic() {
        return TopicBuilder.name(statementDenied).partitions(1).replicas(1).build();
    }
}
