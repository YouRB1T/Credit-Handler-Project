package com.credithandler.dossier.model;

public record GeneratedDocument(
        String fileName,
        String contentType,
        byte[] content
) {
}
