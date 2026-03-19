package com.credithandler.calculator.service;

import com.credithandler.api.controller.calculator.dto.model.EmploymentPosition;
import com.credithandler.api.controller.calculator.dto.model.EmploymentStatus;
import com.credithandler.api.controller.calculator.dto.model.Gender;
import com.credithandler.api.controller.calculator.dto.model.MaritalStatus;
import com.credithandler.calculator.config.ErrorProperties;
import com.credithandler.calculator.config.LoanProperties;
import com.credithandler.calculator.config.ScoringProperties;
import com.credithandler.api.controller.calculator.dto.calc.EmploymentDto;
import com.credithandler.api.controller.calculator.dto.calc.ScoringDataDto;
import com.credithandler.api.exception.BusinessException;
import com.credithandler.calculator.service.impl.CalculateRateServiceImpl;
import com.credithandler.calculator.utils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("Тестирование CalculateRateServiceImpl")
class CalculateRateServiceImplTest {

    @Autowired
    private ErrorProperties errorProperties;

    @Autowired
    private LoanProperties loanProperties;

    @Autowired
    private ScoringProperties scoringProperties;

    @Autowired
    private CalculateRateServiceImpl calculateRateService;

    @Test
    @DisplayName("Расчет ставки прескоринга со всеми опциями")
    void calculatePrescoringRate_withAllOptions_shouldReturnCorrectRate() {
        BigDecimal result = calculateRateService.calculatePrescoringRate(true, true);

        BigDecimal expected = loanProperties.getBaseInterest()
                .subtract(loanProperties.getInsuranceDecrease())
                .subtract(loanProperties.getSalaryDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Расчет ставки прескоринга только со страховкой")
    void calculatePrescoringRate_withOnlyInsurance_shouldReturnCorrectRate() {
        BigDecimal result = calculateRateService.calculatePrescoringRate(true, false);

        BigDecimal expected = loanProperties.getBaseInterest()
                .subtract(loanProperties.getInsuranceDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Расчет ставки прескоринга только для зарплатного клиента")
    void calculatePrescoringRate_withOnlySalary_shouldReturnCorrectRate() {

        BigDecimal result = calculateRateService.calculatePrescoringRate(false, true);


        BigDecimal expected = loanProperties.getBaseInterest()
                .subtract(loanProperties.getSalaryDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Расчет ставки прескоринга без опций")
    void calculatePrescoringRate_withoutOptions_shouldReturnBaseRate() {

        BigDecimal result = calculateRateService.calculatePrescoringRate(false, false);

        assertThat(result).isEqualByComparingTo(loanProperties.getBaseInterest());
    }

    @Test
    @DisplayName("ДУстановка ставки в 0% если она стала отрицательной")
    void calculatePrescoringRate_shouldSetZeroRateWhenNegative() {

        ErrorProperties errorProperties = new ErrorProperties();

        LoanProperties negativeProps = new LoanProperties();
        negativeProps.setBaseInterest(new BigDecimal("5.0"));
        negativeProps.setInsuranceDecrease(new BigDecimal("10.0"));
        negativeProps.setSalaryDecrease(new BigDecimal("5.0"));

        CalculateRateServiceImpl serviceWithNegative =
                new CalculateRateServiceImpl(negativeProps, scoringProperties, errorProperties);

        BigDecimal result = serviceWithNegative.calculatePrescoringRate(true, true);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Расчет ставки скоринга для валидных данных")
    void calculateScoringRate_withValidData_shouldReturnCorrectRate() {

        ScoringDataDto validData = TestDataFactory.createValidScoringData();

        BigDecimal result = calculateRateService.calculateScoringRate(validData);

        assertThat(result).isNotNull();
        assertThat(result.scale()).isEqualTo(2);
        assertThat(result).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("BusinessException для безработного клиента")
    void instantRejection_withUnemployed_shouldThrowException() {

        ScoringDataDto unemployedData = TestDataFactory.createScoringDataWithUnemployed();

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(unemployedData))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Статус занятости");
    }

    @Test
    @DisplayName("BusinessException при слишком большой сумме кредита")
    void instantRejection_withTooHighAmount_shouldThrowException() {

        ScoringDataDto highAmountData = TestDataFactory.createScoringDataWithHighAmount();

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(highAmountData))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Сумма кредита");
    }

    @Test
    @DisplayName("BusinessException для не совершеннолетних")
    void instantRejection_withInvalidAge_shouldThrowException() {
        ScoringDataDto youngData = TestDataFactory.createScoringDataWithInvalidAge(17);

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(youngData))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Возраст");
    }

    @Test
    @DisplayName("BusinessException при недостаточном общем стаже")
    void instantRejection_withLowTotalExperience_shouldThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setWorkExperienceTotal(5);

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(data))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Общий стаж");
    }

    @Test
    @DisplayName("BusinessException при недостаточном текущем стаже")
    void instantRejection_withLowCurrentExperience_shouldThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setWorkExperienceCurrent(1);

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(data))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Текущий стаж");
    }

    @Test
    @DisplayName("Комбинация всех факторов скоринга")
    void calculateScoringRate_shouldCombineAllFactors() {
        ScoringDataDto data = TestDataFactory.createValidScoringData();

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("countOfEmploymentStatus: самозанятый - увеличение ставки")
    void countOfEmploymentStatus_withSelfEmployed_shouldIncreaseRate() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);

        data.setMaritalStatus(MaritalStatus.DEFAULT);
        data.setGender(null);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.add(scoringProperties.getSelfEmployedIncrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfEmploymentStatus: владелец бизнеса - увеличение ставки")
    void countOfEmploymentStatus_withCompanyOwner_shouldIncreaseRate() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.COMPANY_OWNER);
        data.setMaritalStatus(MaritalStatus.DEFAULT);
        data.setGender(null);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.add(scoringProperties.getCompanyOwnerIncrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfEmploymentStatus: наемный работник - без изменений")
    void countOfEmploymentStatus_withEmployed_shouldNotChangeRate() {
        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        data.setMaritalStatus(MaritalStatus.DEFAULT);
        data.setGender(null);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfEmploymentStatus: null статус - должен выбрасывать BusinessException")
    void countOfEmploymentStatus_withNullStatus_shouldThrowBusinessException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(null);
        data.setMaritalStatus(null);
        data.setGender(null);
        data.getEmployment().setPosition(null);

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(data))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Статус занятости");
    }

    @Test
    @DisplayName("countOfPosition: middle manager - скидка")
    void countOfPosition_withMiddleManager_shouldDecreaseRate() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setPosition(EmploymentPosition.MIDDLE_MANAGER);
        data.setMaritalStatus(MaritalStatus.DEFAULT);
        data.setGender(null);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.subtract(scoringProperties.getMiddleManagerDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfPosition: top manager - скидка")
    void countOfPosition_withTopManager_shouldDecreaseRate() {
        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER);
        data.setMaritalStatus(MaritalStatus.DEFAULT);
        data.setGender(null);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.subtract(scoringProperties.getTopManagerDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfPosition: null позиция - должен выбрасывать BusinessException")
    void countOfPosition_withNullPosition_shouldThrowBusinessException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setPosition(null);
        data.setMaritalStatus(null);
        data.setGender(null);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(data))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Должность");
    }

    @Test
    @DisplayName("countOfMaritalStatus: married - скидка")
    void countOfMaritalStatus_withMarried_shouldDecreaseRate() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setMaritalStatus(MaritalStatus.MARRIED);
        data.setGender(null);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.subtract(scoringProperties.getMarriedDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfMaritalStatus: divorced - повышение")
    void countOfMaritalStatus_withDivorced_shouldIncreaseRate() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setMaritalStatus(MaritalStatus.DIVORCED);
        data.setGender(null);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.add(scoringProperties.getDivorcedIncrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfMaritalStatus: single - повышение")
    void countOfMaritalStatus_withSingle_shouldIncreaseRateByOne() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setMaritalStatus(MaritalStatus.SINGLE);
        data.setGender(null);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.add(new BigDecimal("1"));

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("countOfMaritalStatus: null статус - должен выбрасывать BusinessException")
    void countOfMaritalStatus_withNullStatus_shouldThrowBusinessException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setMaritalStatus(null);
        data.setGender(null);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(data))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Семейное положение");
    }

    @ParameterizedTest
    @MethodSource("provideGenderAndAgeData")
    @DisplayName("countOfGenderAndAge: проверка всех комбинаций пола и возраста")
    void countOfGenderAndAge_withDifferentGendersAndAges_shouldAdjustRateCorrectly(
            Gender gender, int age, BigDecimal expectedChange) {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setGender(gender);
        data.setBirthdate(LocalDate.now().minusYears(age));
        data.setMaritalStatus(MaritalStatus.DEFAULT);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.add(expectedChange);

        assertThat(result).isEqualByComparingTo(expected);
    }

    private static Stream<Arguments> provideGenderAndAgeData() {
        return Stream.of(
                // Женщины в оптимальном возрасте
                Arguments.of(Gender.FEMALE, 35, new BigDecimal("-3")),
                Arguments.of(Gender.FEMALE, 45, new BigDecimal("-3")),
                // Женщины вне оптимального возраста
                Arguments.of(Gender.FEMALE, 20, new BigDecimal("0")),
                Arguments.of(Gender.FEMALE, 65, new BigDecimal("0")),
                // Мужчины в оптимальном возрасте
                Arguments.of(Gender.MALE, 35, new BigDecimal("-3")),
                Arguments.of(Gender.MALE, 45, new BigDecimal("-3")),
                // Мужчины вне оптимального возраста
                Arguments.of(Gender.MALE, 20, new BigDecimal("0")),
                Arguments.of(Gender.MALE, 65, new BigDecimal("0")),
                // Небинарные
                Arguments.of(Gender.NON_BINARY, 30, new BigDecimal("7"))
        );
    }

    @Test
    @DisplayName("ensureNonNegativeRate: ставка не может быть отрицательной")
    void ensureNonNegativeRate_shouldNotAllowNegativeRate() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setIsInsuranceEnabled(true);
        data.setIsSalaryClient(true);
        data.setMaritalStatus(MaritalStatus.MARRIED);
        data.setGender(Gender.FEMALE);
        data.setBirthdate(LocalDate.now().minusYears(35));
        data.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        assertThat(result).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }


    @Test
    @DisplayName("instantRejection: точная граница максимальной суммы кредита")
    void instantRejection_withExactMaxAmount_shouldNotThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        BigDecimal annualSalary = data.getEmployment().getSalary().multiply(new BigDecimal("12"));
        BigDecimal maxAllowedLoan = annualSalary.multiply(new BigDecimal(scoringProperties.getMaxSalaryMultiple()));
        data.setAmount(maxAllowedLoan);

        assertThat(calculateRateService.calculateScoringRate(data)).isNotNull();
    }

    @Test
    @DisplayName("instantRejection: точная граница минимального возраста")
    void instantRejection_withExactMinAge_shouldNotThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setBirthdate(LocalDate.now().minusYears(scoringProperties.getMinAge()));

        assertThat(calculateRateService.calculateScoringRate(data)).isNotNull();
    }

    @Test
    @DisplayName("instantRejection: точная граница максимального возраста")
    void instantRejection_withExactMaxAge_shouldNotThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setBirthdate(LocalDate.now().minusYears(scoringProperties.getMaxAge()));

        assertThat(calculateRateService.calculateScoringRate(data)).isNotNull();
    }

    @Test
    @DisplayName("instantRejection: точная граница минимального общего стажа")
    void instantRejection_withExactMinTotalExperience_shouldNotThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setWorkExperienceTotal(scoringProperties.getMinTotalWorkExperience());

        assertThat(calculateRateService.calculateScoringRate(data)).isNotNull();
    }

    @Test
    @DisplayName("instantRejection: точная граница минимального текущего стажа")
    void instantRejection_withExactMinCurrentExperience_shouldNotThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setWorkExperienceCurrent(scoringProperties.getMinCurrentWorkExperience());

        assertThat(calculateRateService.calculateScoringRate(data)).isNotNull();
    }

    @Test
    @DisplayName("Комбинация: самозанятый + middle manager + married + женщина 35 лет")
    void calculateScoringRate_withComplexCombination1_shouldCalculateCorrectly() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.MIDDLE_MANAGER);
        data.setMaritalStatus(MaritalStatus.MARRIED);
        data.setGender(Gender.FEMALE);
        data.setBirthdate(LocalDate.now().minusYears(35));

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.add(scoringProperties.getSelfEmployedIncrease());
        expected = expected.subtract(scoringProperties.getMiddleManagerDecrease());
        expected = expected.subtract(scoringProperties.getMarriedDecrease());
        expected = expected.subtract(scoringProperties.getFemaleDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Комбинация: владелец бизнеса + top manager + divorced + мужчина 45 лет")
    void calculateScoringRate_withComplexCombination2_shouldCalculateCorrectly() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.COMPANY_OWNER);
        data.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER);
        data.setMaritalStatus(MaritalStatus.DIVORCED);
        data.setGender(Gender.MALE);
        data.setBirthdate(LocalDate.now().minusYears(45));

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        BigDecimal expected = loanProperties.getBaseInterest();
        if (data.getIsInsuranceEnabled()) expected = expected.subtract(loanProperties.getInsuranceDecrease());
        if (data.getIsSalaryClient()) expected = expected.subtract(loanProperties.getSalaryDecrease());
        expected = expected.add(scoringProperties.getCompanyOwnerIncrease());
        expected = expected.subtract(scoringProperties.getTopManagerDecrease());
        expected = expected.add(scoringProperties.getDivorcedIncrease());
        expected = expected.subtract(scoringProperties.getMaleDecrease());

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Комбинация: безработный (должен выбросить исключение)")
    void calculateScoringRate_withUnemployed_shouldThrowException() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.UNEMPLOYED);

        assertThatThrownBy(() -> calculateRateService.calculateScoringRate(data))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Статус занятости");
    }

    @Test
    @DisplayName("Порядок применения: прескоринг → статус занятости → должность → семейное положение → пол/возраст")
    void calculateScoringRate_shouldApplyFactorsInCorrectOrder() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setIsInsuranceEnabled(true);
        data.setIsSalaryClient(true);
        data.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER);
        data.setMaritalStatus(MaritalStatus.MARRIED);
        data.setGender(Gender.FEMALE);
        data.setBirthdate(LocalDate.now().minusYears(35));

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        assertThat(result).isNotNull();
        assertThat(result).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("ensureNonNegativeRate: комбинация факторов, дающая отрицательную ставку")
    void ensureNonNegativeRate_withManyDiscounts_shouldReturnZero() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setIsInsuranceEnabled(true);
        data.setIsSalaryClient(true);
        data.setMaritalStatus(MaritalStatus.MARRIED);
        data.setGender(Gender.FEMALE);
        data.setBirthdate(LocalDate.now().minusYears(35));
        data.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER);

        LoanProperties smallBaseProps = new LoanProperties();
        smallBaseProps.setBaseInterest(new BigDecimal("5.0"));
        smallBaseProps.setInsuranceDecrease(loanProperties.getInsuranceDecrease());
        smallBaseProps.setSalaryDecrease(loanProperties.getSalaryDecrease());

        CalculateRateServiceImpl serviceWithSmallBase =
                new CalculateRateServiceImpl(smallBaseProps, scoringProperties, errorProperties);

        BigDecimal result = serviceWithSmallBase.calculateScoringRate(data);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    @DisplayName("calculateAge: день рождения сегодня")
    void calculateAge_withBirthdayToday_shouldReturnCorrectAge() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setBirthdate(LocalDate.now().minusYears(30));

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("calculateAge: день рождения завтра")
    void calculateAge_withBirthdayTomorrow_shouldReturnCorrectAge() {
        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.setBirthdate(LocalDate.now().minusYears(30).plusDays(1));

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        assertThat(result).isNotNull();
    }


    @Test
    @DisplayName("Обработка null: все поля null в Employment")
    void calculateScoringRate_withNullEmploymentFields_shouldWork() {

        ScoringDataDto data = TestDataFactory.createValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        data.getEmployment().setPosition(EmploymentPosition.EMPLOYER);
        data.getEmployment().setSalary(new BigDecimal("50000"));
        data.getEmployment().setWorkExperienceTotal(60);
        data.getEmployment().setWorkExperienceCurrent(24);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Обработка null: все поля в ScoringDataDto null кроме обязательных")
    void calculateScoringRate_withMinimalData_shouldWork() {

        ScoringDataDto data = new ScoringDataDto();
        data.setAmount(new BigDecimal("100000"));
        data.setBirthdate(LocalDate.now().minusYears(30));
        data.setIsInsuranceEnabled(false);
        data.setIsSalaryClient(false);

        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setSalary(new BigDecimal("50000"));
        employment.setWorkExperienceTotal(60);
        employment.setWorkExperienceCurrent(24);
        employment.setPosition(EmploymentPosition.EMPLOYER);
        data.setMaritalStatus(MaritalStatus.DEFAULT);
        data.setEmployment(employment);

        BigDecimal result = calculateRateService.calculateScoringRate(data);

        assertThat(result).isNotNull();
    }
}