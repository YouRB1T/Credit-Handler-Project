package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.deal.model.Statement;
import com.credithandler.deal.model.enums.ApplicationStatus;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.impl.DealServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест для проверки пессимистичной блокировки
 * (PESSIMISTIC_WRITE) в методе selectOfferForDeal.
 *
 * <p>Сценарий: два параллельных вызова selectOfferForDeal для одной и той же
 * заявки. Ожидается, что только первый вызов успешно обработает заявку,
 * а второй — получит ошибку некорректного статуса (APPROVED вместо PREAPPROVAL),
 * так как будет ждать завершения первой транзакции и увидит уже изменённый статус.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext
class SelectOfferForDealConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DealServiceImpl dealService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Statement testStatement;
    private LoanOfferDto testOffer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @BeforeEach
    void setUp() {
        // Создаём заявку в статусе PREAPPROVAL
        testStatement = new Statement();
        testStatement.setStatus(ApplicationStatus.PREAPPROVAL);
        testStatement.setHistoryStatus(new ArrayList<>());
        testStatement = statementRepository.save(testStatement);

        // Создаём тестовое предложение
        testOffer = new LoanOfferDto();
        testOffer.setStatementId(testStatement.getStatementId());
        testOffer.setRequestedAmount(BigDecimal.valueOf(100000));
        testOffer.setTotalAmount(BigDecimal.valueOf(110000));
        testOffer.setTerm(12);
        testOffer.setMonthlyPayment(BigDecimal.valueOf(9166.67));
        testOffer.setRate(BigDecimal.valueOf(12.5));
        testOffer.setIsInsuranceEnabled(false);
        testOffer.setIsSalaryClient(false);
    }

    @AfterEach
    void tearDown() {
        statementRepository.deleteAll();
    }

    @Test
    @DisplayName("Конкурентный вызов selectOfferForDeal — только один поток успешно меняет статус")
    void concurrentSelectOffer_onlyOneSucceeds() throws Exception {
        UUID statementId = testStatement.getStatementId();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicReference<StatementDto> successResult = new AtomicReference<>();
        AtomicReference<Throwable> failureException = new AtomicReference<>();
        int[] successCount = {0};

        // Создаём два одинаковых оффера для одного statementId
        LoanOfferDto offer1 = new LoanOfferDto();
        offer1.setStatementId(statementId);
        offer1.setRequestedAmount(BigDecimal.valueOf(100000));
        offer1.setTotalAmount(BigDecimal.valueOf(110000));
        offer1.setTerm(12);
        offer1.setMonthlyPayment(BigDecimal.valueOf(9166.67));
        offer1.setRate(BigDecimal.valueOf(12.5));
        offer1.setIsInsuranceEnabled(false);
        offer1.setIsSalaryClient(false);

        LoanOfferDto offer2 = new LoanOfferDto();
        offer2.setStatementId(statementId);
        offer2.setRequestedAmount(BigDecimal.valueOf(200000));
        offer2.setTotalAmount(BigDecimal.valueOf(220000));
        offer2.setTerm(24);
        offer2.setMonthlyPayment(BigDecimal.valueOf(9166.67));
        offer2.setRate(BigDecimal.valueOf(12.5));
        offer2.setIsInsuranceEnabled(false);
        offer2.setIsSalaryClient(false);

        // Поток 1 — вызывает selectOfferForDeal
        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                StatementDto result = dealService.selectOfferForDeal(offer1);
                successResult.set(result);
                synchronized (successCount) {
                    successCount[0]++;
                }
            } catch (Throwable e) {
                failureException.compareAndSet(null, e);
            } finally {
                doneLatch.countDown();
            }
        });

        // Поток 2 — вызывает selectOfferForDeal для той же заявки
        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                StatementDto result = dealService.selectOfferForDeal(offer2);
                successResult.set(result);
                synchronized (successCount) {
                    successCount[0]++;
                }
            } catch (Throwable e) {
                failureException.compareAndSet(null, e);
            } finally {
                doneLatch.countDown();
            }
        });

        // Ждём, пока оба потока будут готовы
        readyLatch.await();
        // Одновременно запускаем оба потока
        startLatch.countDown();
        // Ждём завершения обоих
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(finished).isTrue();

        // Один вызов должен пройти успешно
        assertThat(successCount[0]).isEqualTo(1);

        // Второй вызов должен получить ошибку (статус уже не PREAPPROVAL)
        assertThat(failureException.get()).isNotNull();

        // Проверяем, что в БД статус изменён только один раз — на APPROVED
        Statement updatedStatement = statementRepository.findById(statementId).orElseThrow();
        assertThat(updatedStatement.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(updatedStatement.getAppliedOffer()).isNotNull();
    }

    @Test
    @DisplayName("Пессимистичная блокировка — второй поток ожидает завершения первого")
    void pessimisticLock_secondThreadWaitsForFirst() throws Exception {
        UUID statementId = testStatement.getStatementId();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        // Поток 1: открывает транзакцию, блокирует строку и держит блокировку
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch firstThreadDone = new CountDownLatch(1);
        CountDownLatch secondThreadDone = new CountDownLatch(1);

        Thread thread1 = new Thread(() -> {
            txTemplate.execute(status -> {
                // acquire pessimistic lock
                statementRepository.findByIdWithLock(statementId);
                lockAcquired.countDown();

                // Hold lock for 2 seconds
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            firstThreadDone.countDown();
        });

        AtomicReference<Long> secondThreadStart = new AtomicReference<>();
        AtomicReference<Long> secondThreadDuration = new AtomicReference<>();

        Thread thread2 = new Thread(() -> {
            try {
                // Wait until thread1 has acquired the lock
                lockAcquired.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            long start = System.currentTimeMillis();
            secondThreadStart.set(start);

            txTemplate.execute(status -> {
                // This should block until thread1 releases the lock
                statementRepository.findByIdWithLock(statementId);
                return null;
            });

            long duration = System.currentTimeMillis() - start;
            secondThreadDuration.set(duration);
            secondThreadDone.countDown();
        });

        thread1.start();
        thread2.start();

        assertThat(firstThreadDone.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(secondThreadDone.await(10, TimeUnit.SECONDS)).isTrue();

        // Поток 2 должен был ждать как минимум 1.5 секунды
        // (поток 1 держал блокировку 2 секунды)
        assertThat(secondThreadDuration.get()).isGreaterThanOrEqualTo(1500L);
    }
}