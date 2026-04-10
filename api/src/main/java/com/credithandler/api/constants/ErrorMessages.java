package com.credithandler.api.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {
    public static final String ERROR_RESPONSE_TIMESTAMP = "timestamp";
    public static final String ERROR_RESPONSE_STATUS = "status";
    public static final String ERROR_RESPONSE_ERROR = "error";
    public static final String ERROR_RESPONSE_MESSAGE = "message";
    public static final String ERROR_RESPONSE_FIELD = "field";
    public static final String ERROR_RESPONSE_ERRORS = "errors";

    public static final String ERROR_BUSINESS_TITLE = "Бизнес ошибка";
    public static final String ERROR_VALIDATION_TITLE = "Ошибка валидации";
    public static final String ERROR_SERVER_TITLE = "Внутренняя ошибка сервера";
    public static final String ERROR_SERVER_MESSAGE = "Произошла непредвиденная ошибка";
}
