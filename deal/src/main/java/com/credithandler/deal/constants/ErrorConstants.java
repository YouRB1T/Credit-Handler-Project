package com.credithandler.deal.constants;

public class ErrorConstants {

    public static final String CLIENT_NOT_CREATED = "Клиент не был создан";
    public static final String CLIENT_NOT_CREATED_DESC = "Не удалось создать клиента по переданному запросу";

    public static final String CLIENT_NOT_FOUND = "Клиент не найден";
    public static final String CLIENT_NOT_FOUND_DESC = "Клиент с ID %s не найден";

    public static final String STATEMENT_NOT_CREATED = "Заявка не была создана";
    public static final String STATEMENT_NOT_CREATED_DESC = "Ошибка при создании заявки";

    public static final String STATEMENT_NOT_FOUND = "Заявка не найдена";
    public static final String STATEMENT_NOT_FOUND_DESC = "Заявка с ID %s не найдена";

    public static final String INVALID_STATEMENT_STATUS = "Некорректный статус заявки";
    public static final String INVALID_STATEMENT_STATUS_DESC = "Заявка уже находится в статусе %s и не может быть обработана";

    public static final String OFFERS_NOT_FOUND = "Предложения не получены";
    public static final String OFFERS_NOT_FOUND_DESC = "Калькулятор не вернул ни одного предложения";

    public static final String CREDIT_NOT_CALCULATED = "Ошибка расчёта кредита";
    public static final String CREDIT_NOT_CALCULATED_DESC = "Калькулятор не вернул данные по кредиту";

}
