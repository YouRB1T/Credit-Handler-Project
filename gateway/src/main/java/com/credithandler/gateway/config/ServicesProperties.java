package com.credithandler.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway.services")
public class ServicesProperties {

    private String dealUrl;

    private String statementUrl;

    public String getDealUrl() {
        return dealUrl;
    }

    public void setDealUrl(String dealUrl) {
        this.dealUrl = dealUrl;
    }

    public String getStatementUrl() {
        return statementUrl;
    }

    public void setStatementUrl(String statementUrl) {
        this.statementUrl = statementUrl;
    }
}
