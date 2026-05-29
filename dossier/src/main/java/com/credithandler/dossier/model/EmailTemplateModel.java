package com.credithandler.dossier.model;

import java.util.List;
import java.util.UUID;

public record EmailTemplateModel(
        String subject,
        String preheader,
        String badge,
        String title,
        String lead,
        UUID statementId,
        String status,
        List<EmailDetail> details,
        String nextStepTitle,
        String nextStepText,
        String code,
        List<String> documents,
        String securityNote
) {
}
