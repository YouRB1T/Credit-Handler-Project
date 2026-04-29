package com.credithandler.dossier.model;

public record GeneratedDocument(
        String fileName,
        String attachmentName,
        String contentType,
        byte[] content
) {
}
