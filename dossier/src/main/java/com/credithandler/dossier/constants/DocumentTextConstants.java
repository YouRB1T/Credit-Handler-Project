package com.credithandler.dossier.constants;

public final class DocumentTextConstants {

    public static final String CREDIT_CONTRACT_TITLE = "Кредитный договор";
    public static final String PAYMENT_SCHEDULE_TITLE = "График платежей";
    public static final String INDIVIDUAL_CONDITIONS_TITLE = "Индивидуальные условия кредитования";

    public static final String STATEMENT_ID = "Номер заявки: %s";
    public static final String CREATED_AT = "Дата формирования: %s";
    public static final String CLIENT_ID = "Идентификатор клиента: %s";
    public static final String CREDIT_ID = "Идентификатор кредита: %s";
    public static final String STATEMENT_STATUS = "Статус заявки: %s";
    public static final String REQUESTED_AMOUNT = "Сумма кредита: %s";
    public static final String CREDIT_TERM = "Срок кредита: %s месяцев";
    public static final String INTEREST_RATE = "Процентная ставка: %s";
    public static final String MONTHLY_PAYMENT = "Ежемесячный платеж: %s";
    public static final String TOTAL_AMOUNT = "Полная сумма к возврату: %s";
    public static final String INSURANCE_ENABLED = "Страхование подключено: %s";
    public static final String SALARY_CLIENT = "Зарплатный клиент: %s";

    public static final String CREDIT_CONTRACT_FOOTER =
            "Документ сформирован автоматически для кредитной заявки клиента.";
    public static final String PAYMENT_SCHEDULE_FOOTER =
            "График платежей сформирован на основании выбранного кредитного предложения.";
    public static final String INDIVIDUAL_CONDITIONS_FOOTER =
            "Индивидуальные условия сформированы на основании выбранного кредитного предложения.";

    private DocumentTextConstants() {
    }
}
