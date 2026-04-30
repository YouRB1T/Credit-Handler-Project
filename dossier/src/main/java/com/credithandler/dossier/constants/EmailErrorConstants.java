package com.credithandler.dossier.constants;

public final class EmailErrorConstants {

    public static final String EMAIL_SEND_ERROR = "Ошибка отправки письма";
    public static final String EMAIL_SEND_ERROR_DESCRIPTION = "Не удалось отправить письмо на адрес %s";
    public static final String EMAIL_RECIPIENT_NOT_REACHED_ERROR = "Письмо не доставлено получателю";
    public static final String EMAIL_RECIPIENT_NOT_REACHED_ERROR_DESCRIPTION =
            "SMTP-сервер отклонил отправку письма на адрес %s";
    public static final String EMAIL_MESSAGE_CREATION_ERROR = "Ошибка формирования письма";
    public static final String EMAIL_MESSAGE_CREATION_ERROR_DESCRIPTION =
            "Не удалось сформировать письмо с кредитными документами для адреса %s";

    private EmailErrorConstants() {
    }
}
