package com.credithandler.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("dealRestClient")
    public RestClient dealRestClient(ServicesProperties servicesProperties) {
        return RestClient.builder()
                .baseUrl(servicesProperties.getDealUrl())
                .build();
    }

    @Bean
    @Qualifier("statementRestClient")
    public RestClient statementRestClient(ServicesProperties servicesProperties) {
        return RestClient.builder()
                .baseUrl(servicesProperties.getStatementUrl())
                .build();
    }
}
