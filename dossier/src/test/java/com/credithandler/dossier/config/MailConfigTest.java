package com.credithandler.dossier.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("Тестирование почтовой конфигурации")

class MailConfigTest {

    @Test
    @DisplayName("Почтовые настройки связываются через ConfigurationProperties")
    void mailPropertiesAreBoundThroughConfigurationProperties() {
        MailProperties properties = new MailProperties();

        new Binder(new MapConfigurationPropertySource(Map.of(
                "spring.mail.host", "smtp.example.test",
                "spring.mail.port", "2525",
                "spring.mail.username", "user",
                "spring.mail.password", "secret",
                "spring.mail.properties.mail.smtp.auth", "true",
                "spring.mail.properties.mail.smtp.starttls.enable", "true",
                "spring.mail.properties.mail.smtp.ssl.enable", "false"
        ))).bind("spring.mail", Bindable.ofInstance(properties));

        JavaMailSenderImpl sender = (JavaMailSenderImpl) new MailConfig(properties).javaMailSender();

        assertThat(sender.getHost()).isEqualTo("smtp.example.test");
        assertThat(sender.getPort()).isEqualTo(2525);
        assertThat(sender.getUsername()).isEqualTo("user");
        assertThat(sender.getPassword()).isEqualTo("secret");
        assertThat(sender.getJavaMailProperties())
                .containsEntry("mail.smtp.auth", "true")
                .containsEntry("mail.smtp.starttls.enable", "true")
                .containsEntry("mail.smtp.ssl.enable", "false");
    }

    @Test
    @DisplayName("MailConfig не использует внедрение через Value")
    void mailConfigDoesNotUseValueInjection() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/credithandler/dossier/config/MailConfig.java"
        ));

        assertThat(source).doesNotContain("@Value");
    }
}
