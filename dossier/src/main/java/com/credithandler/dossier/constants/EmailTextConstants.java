package com.credithandler.dossier.constants;

public final class EmailTextConstants {

    public static final String FINISH_REGISTRATION_SUBJECT = "Завершение регистрации кредитной заявки";
    public static final String CREATE_DOCUMENTS_SUBJECT = "Формирование кредитных документов";
    public static final String SEND_DOCUMENTS_SUBJECT = "Кредитные документы по вашей заявке";
    public static final String SEND_SES_SUBJECT = "Код подтверждения подписания документов";
    public static final String CREDIT_ISSUED_SUBJECT = "Кредит выдан";
    public static final String STATEMENT_DENIED_SUBJECT = "Отказ по кредитной заявке";

    public static final String FINISH_REGISTRATION_TEXT = """
            Здравствуйте!

            Ваша кредитная заявка предварительно одобрена.
            Для продолжения оформления кредита завершите регистрацию и заполните дополнительные данные.

            Номер заявки: %s

            %s
            """;

    public static final String CREATE_DOCUMENTS_TEXT = """
            Здравствуйте!

            По вашей кредитной заявке сформированы кредитные документы.
            Следующим шагом запросите отправку документов для ознакомления и подписания.

            Номер заявки: %s

            Статус заявки: %s
            """;

    public static final String SIMPLE_TEXT = """
            Здравствуйте!

            %s

            Номер заявки: %s
            """;

    public static final String CREDIT_ISSUED_TEXT = "Поздравляем, кредит успешно выдан.";
    public static final String STATEMENT_DENIED_TEXT = "По вашей кредитной заявке принято отрицательное решение.";

    public static final String SEND_DOCUMENTS_TEXT = """
            Здравствуйте!

            Кредитные документы по вашей заявке сформированы и приложены к письму.
            Ознакомьтесь с документами и перейдите к подписанию.

            Номер заявки: %s

            Статус заявки: %s
            """;

    private EmailTextConstants() {
    }
}
