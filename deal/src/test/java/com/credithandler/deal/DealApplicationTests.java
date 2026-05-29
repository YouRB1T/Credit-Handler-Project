package com.credithandler.deal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Тестирование запуска deal")
class DealApplicationTests {

    @Test
    @DisplayName("Контекст приложения deal загружается")
    void contextLoads() {
    }

}
