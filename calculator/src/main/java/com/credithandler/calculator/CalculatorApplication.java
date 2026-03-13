package com.credithandler.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//TODO: Добавить обработчик ошибок для controller
//TODO: Добавить тесты на котнроллер -интиграционные, на сервисы - unit и модульные
@SpringBootApplication
public class CalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalculatorApplication.class, args);
    }

}
