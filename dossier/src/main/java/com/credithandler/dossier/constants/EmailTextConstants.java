package com.credithandler.dossier.constants;

public final class EmailTextConstants {

    public static final String CREATE_DOCUMENTS_TEXT = """
            Здравствуйте!

            По вашей кредитной заявке сформированы кредитные документы.
            Следующим шагом запросите отправку документов для ознакомления и подписания.

            Номер заявки: %s

            Статус заявки: %s
            """;

    private EmailTextConstants() {
    }
}
