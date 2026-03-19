package com.credithandler.calculator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "error")
public class ErrorProperties {

    private Integer businessCode;
    private Integer validationCode;
    private Integer serverCode;

    private String unemployedMessage;
    private String loanAmountMessage;
    private String ageMessage;
    private String totalExperienceMessage;
    private String currentExperienceMessage;
    private String employmentStatusNullMessage;
    private String positionNullMessage;
    private String maritalStatusNullMessage;
    private String birthdateNullMessage;
    private String zeroRateMessage;
    private String negativeRateMessage;
    private String invalidTermMessage;
    private String invalidAmountMessage;

    private String logEmploymentStatus;
    private String logLoanAmount;
    private String logAge;
    private String logTotalExperience;
    private String logCurrentExperience;
    private String logSelfEmployed;
    private String logCompanyOwner;
    private String logMiddleManager;
    private String logTopManager;
    private String logMarried;
    private String logDivorced;
    private String logFemaleDiscount;
    private String logMaleDiscount;
    private String logNonBinaryIncrease;
}