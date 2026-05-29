package com.credithandler.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("Тестирование конфигурации OpenAPI gateway")

class OpenApiConfigTest {

    @Test
    @DisplayName("OpenApiConfig описывает API gateway")
    void openApiConfigDescribesGatewayApi() {
        var openAPI = new OpenApiConfig().gatewayOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Credit Handler Gateway API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1");
    }
}
