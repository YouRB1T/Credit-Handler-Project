package com.credithandler.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("Тестирование настроек сервисов gateway")

class ServicesPropertiesTest {

    @Test
    @DisplayName("URL сервисов gateway связываются через configuration properties")
    void gatewayServiceUrlsAreBoundFromConfigurationProperties() {
        ServicesProperties properties = new ServicesProperties();

        new Binder(new MapConfigurationPropertySource(Map.of(
                "gateway.services.deal-url", "http://localhost:8082",
                "gateway.services.statement-url", "http://localhost:8083"
        ))).bind("gateway.services", Bindable.ofInstance(properties));

        assertThat(properties.getDealUrl()).isEqualTo("http://localhost:8082");
        assertThat(properties.getStatementUrl()).isEqualTo("http://localhost:8083");
    }
}
