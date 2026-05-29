package com.credithandler.dossier.model;

public record RenderedEmail(
        String subject,
        String plainText,
        String htmlText
) {
}
