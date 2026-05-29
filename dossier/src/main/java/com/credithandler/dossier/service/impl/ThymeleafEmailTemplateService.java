package com.credithandler.dossier.service.impl;

import com.credithandler.dossier.model.EmailDetail;
import com.credithandler.dossier.model.EmailTemplateModel;
import com.credithandler.dossier.model.RenderedEmail;
import com.credithandler.dossier.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ThymeleafEmailTemplateService implements EmailTemplateService {

    private static final String TEMPLATE_NAME = "email/credit-notification";
    private static final Locale RUSSIAN_LOCALE = Locale.forLanguageTag("ru");

    private final SpringTemplateEngine templateEngine;

    @Override
    public RenderedEmail render(EmailTemplateModel model) {
        Context context = new Context(RUSSIAN_LOCALE);
        List<EmailDetail> details = safeList(model.details());
        List<String> documents = safeList(model.documents());

        context.setVariable("subject", model.subject());
        context.setVariable("preheader", model.preheader());
        context.setVariable("badge", model.badge());
        context.setVariable("title", model.title());
        context.setVariable("lead", model.lead());
        context.setVariable("statementId", model.statementId());
        context.setVariable("status", model.status());
        context.setVariable("details", details);
        context.setVariable("hasDetails", !details.isEmpty());
        context.setVariable("nextStepTitle", model.nextStepTitle());
        context.setVariable("nextStepText", model.nextStepText());
        context.setVariable("code", model.code());
        context.setVariable("documents", documents);
        context.setVariable("hasDocuments", !documents.isEmpty());
        context.setVariable("securityNote", model.securityNote());

        String htmlText = templateEngine.process(TEMPLATE_NAME, context);
        return new RenderedEmail(model.subject(), buildPlainText(model, details, documents), htmlText);
    }

    private String buildPlainText(EmailTemplateModel model, List<EmailDetail> details, List<String> documents) {
        StringBuilder text = new StringBuilder();
        appendLine(text, model.title());
        appendBlankLine(text);
        appendLine(text, model.lead());
        appendBlankLine(text);

        if (model.statementId() != null) {
            appendLine(text, "Номер заявки: " + model.statementId());
        }
        if (hasText(model.status())) {
            appendLine(text, "Статус: " + model.status());
        }
        for (EmailDetail detail : details) {
            if (hasText(detail.label()) && hasText(detail.value())) {
                appendLine(text, detail.label() + ": " + detail.value());
            }
        }

        if (hasText(model.code())) {
            appendBlankLine(text);
            appendLine(text, "Код подтверждения: " + model.code());
        }

        if (!documents.isEmpty()) {
            appendBlankLine(text);
            appendLine(text, "Документы во вложении:");
            documents.forEach(document -> appendLine(text, "- " + document));
        }

        if (hasText(model.nextStepTitle()) || hasText(model.nextStepText())) {
            appendBlankLine(text);
            appendLine(text, model.nextStepTitle());
            appendLine(text, model.nextStepText());
        }

        if (hasText(model.securityNote())) {
            appendBlankLine(text);
            appendLine(text, "Безопасность: " + model.securityNote());
        }

        return text.toString().trim();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void appendLine(StringBuilder text, String value) {
        if (hasText(value)) {
            text.append(value).append(System.lineSeparator());
        }
    }

    private void appendBlankLine(StringBuilder text) {
        if (!text.isEmpty() && !text.toString().endsWith(System.lineSeparator().repeat(2))) {
            text.append(System.lineSeparator());
        }
    }
}
