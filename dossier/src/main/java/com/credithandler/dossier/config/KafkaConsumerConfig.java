package com.credithandler.dossier.config;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, EmailMessage> emailMessageConsumerFactory() {
        JsonDeserializer<EmailMessage> valueDeserializer = new JsonDeserializer<>(EmailMessage.class);
        valueDeserializer.addTrustedPackages("com.credithandler.api.dto.dossier");
        valueDeserializer.setUseTypeHeaders(false);

        Map<String, Object> config = commonConsumerConfig();

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConsumerFactory<String, StatementDto> statementDtoConsumerFactory() {
        JsonDeserializer<StatementDto> valueDeserializer = new JsonDeserializer<>(StatementDto.class);
        valueDeserializer.addTrustedPackages(
                "com.credithandler.api.dto.model",
                "com.credithandler.api.dto.loan"
        );
        valueDeserializer.setUseTypeHeaders(false);

        Map<String, Object> config = commonConsumerConfig();

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EmailMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmailMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailMessageConsumerFactory());
        return factory;
    }

    @Bean(name = "statementDtoKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, StatementDto> statementDtoKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StatementDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(statementDtoConsumerFactory());
        return factory;
    }

    private Map<String, Object> commonConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return config;
    }
}
