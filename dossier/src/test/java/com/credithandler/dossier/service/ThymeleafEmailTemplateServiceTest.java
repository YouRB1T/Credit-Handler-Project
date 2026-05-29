package com.credithandler.dossier.service;

import com.credithandler.dossier.model.EmailDetail;
import com.credithandler.dossier.model.EmailTemplateModel;
import com.credithandler.dossier.model.RenderedEmail;
import com.credithandler.dossier.service.impl.ThymeleafEmailTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ThymeleafEmailTemplateService")
class ThymeleafEmailTemplateServiceTest {

    @Test
    @DisplayName("renders readable bank-style HTML email with plain-text fallback")
    void render_shouldBuildHtmlEmailAndPlainTextFallback() {
        ThymeleafEmailTemplateService service = new ThymeleafEmailTemplateService(templateEngine());
        UUID statementId = UUID.randomUUID();

        RenderedEmail renderedEmail = service.render(new EmailTemplateModel(
                "Кредитные документы по вашей заявке",
                "Документы готовы и приложены к письму.",
                "Документы",
                "Кредитные документы готовы",
                "Мы подготовили документы по вашей заявке. Проверьте вложения перед подписанием.",
                statementId,
                "DOCUMENTS_CREATED",
                List.of(new EmailDetail("Получатель", "client@example.com")),
                "Что дальше",
                "Ознакомьтесь с PDF-файлами во вложении и переходите к подписанию в сервисе.",
                null,
                List.of("credit-contract.pdf", "payment-schedule.pdf"),
                "Банк никогда не попросит переслать код подтверждения или пароль в ответном письме."
        ));

        assertThat(renderedEmail.subject()).isEqualTo("Кредитные документы по вашей заявке");
        assertThat(renderedEmail.htmlText())
                .contains("<!DOCTYPE html>")
                .contains("Credit Handler")
                .contains("Кредитные документы готовы")
                .contains(statementId.toString())
                .contains("credit-contract.pdf")
                .contains("Что дальше")
                .doesNotContain("th:text")
                .doesNotContain("th:if")
                .doesNotContain("xmlns:th");
        assertThat(renderedEmail.plainText())
                .contains("Кредитные документы готовы")
                .contains("Номер заявки: " + statementId)
                .contains("credit-contract.pdf");
    }

    private SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }
}
