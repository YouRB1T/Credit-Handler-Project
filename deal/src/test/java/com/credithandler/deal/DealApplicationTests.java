package com.credithandler.deal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Тестирование запуска deal")
class DealApplicationTests extends PostgresIntegrationTest {

    @Test
    @DisplayName("Контекст приложения deal загружается")
    void contextLoads() {
    }

}
