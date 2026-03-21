package com.credithandler.calculator.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

    public static final String ERROR_UNEMPLOYED_MESSAGE = "Клиент безработный - кредит не может быть выдан";
    public static final String ERROR_LOAN_AMOUNT_MESSAGE = "Сумма кредита превышает максимально допустимую на основе зарплаты";
    public static final String ERROR_AGE_MESSAGE = "Возраст клиента выходит за допустимые пределы";
    public static final String ERROR_TOTAL_EXPERIENCE_MESSAGE = "Общий стаж работы меньше требуемого";
    public static final String ERROR_CURRENT_EXPERIENCE_MESSAGE = "Текущий стаж работы меньше требуемого";

    public static final String ERROR_LOG_EMPLOYMENT_STATUS = "Статус занятости";
    public static final String ERROR_LOG_LOAN_AMOUNT = "Сумма кредита";
    public static final String ERROR_LOG_AGE = "Возраст";
    public static final String ERROR_LOG_TOTAL_EXPERIENCE = "Общий стаж работы";
    public static final String ERROR_LOG_CURRENT_EXPERIENCE = "Текущий стаж работы";
    public static final String ERROR_LOG_SELF_EMPLOYED = "Применено повышение ставки для самозанятого: +{}%";
    public static final String ERROR_LOG_COMPANY_OWNER = "Применено повышение ставки для владельца бизнеса: +{}%";
    public static final String ERROR_LOG_MIDDLE_MANAGER = "Применена скидка для руководителя среднего звена: -{}%";
    public static final String ERROR_LOG_TOP_MANAGER = "Применена скидка для топ-менеджера: -{}%";
    public static final String ERROR_LOG_MARRIED = "Применена скидка для женатых/замужем: -{}%";
    public static final String ERROR_LOG_DIVORCED = "Применено повышение ставки для разведенных: +{}%";
    public static final String ERROR_LOG_FEMALE_DISCOUNT = "Применена скидка для женщины {}-{} лет: -{}%";
    public static final String ERROR_LOG_MALE_DISCOUNT = "Применена скидка для мужчины {}-{} лет: -{}%";
    public static final String ERROR_LOG_NON_BINARY_INCREASE = "Применено повышение ставки для небинарных: +{}%";
}
