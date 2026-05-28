package com.credithandler.dossier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
@DisplayName("Тестирование запуска dossier")
class DossierApplicationTests {

    @Test
    @DisplayName("Контекст приложения dossier загружается")
    void contextLoads() {
    }

}
