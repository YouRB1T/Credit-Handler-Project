package com.credithandler.deal.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${kafka.topic.finish-registration}")
    private String finishRegistrationTopic;

    @Value("${kafka.topic.create-documents}")
    private String createDocumentsTopic;

    @Value("${kafka.topic.send-documents}")
    private String sendDocumentsTopic;

    @Value("${kafka.topic.send-ses}")
    private String sendSesTopic;

    @Value("${kafka.topic.credit-issued}")
    private String creditIssuedTopic;

    @Value("${kafka.topic.statement-denied}")
    private String statementDeniedTopic;

    @Bean
    public NewTopic finishRegistrationTopic() {
        return TopicBuilder.name(finishRegistrationTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic createDocumentsTopic() {
        return TopicBuilder.name(createDocumentsTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sendDocumentsTopic() {
        return TopicBuilder.name(sendDocumentsTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sendSesTopic() {
        return TopicBuilder.name(sendSesTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic creditIssuedTopic() {
        return TopicBuilder.name(creditIssuedTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic statementDeniedTopic() {
        return TopicBuilder.name(statementDeniedTopic).partitions(1).replicas(1).build();
    }
}
