package com.credithandler.deal.lock;

import com.credithandler.deal.model.Statement;
import com.credithandler.deal.repository.StatementRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Тестирование пессимистичной блокировки заявок")
class StatementRepositoryPessimisticLockTest {

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    // Нужен для создания отдельных транзакций в разных потоках
    private PlatformTransactionManager transactionManager;

    // Пул потоков, в нем создаю потоки
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    @DisplayName("Второй вызов ждет освобождения блокировки первой транзакцией")
    void secondCallShouldWaitUntilFirstTransactionReleasesLock() throws Exception {

        UUID finalCreditId = UUID.randomUUID();

        Statement statement = new Statement();
        statement.setCreditId(UUID.randomUUID());

        // быстрое и принудительное сохранение
        Statement statementSave = statementRepository.saveAndFlush(statement);

        UUID businessId = statementSave.getStatementId();

        // Ожидание для первого потока
        CountDownLatch firstLocked = new CountDownLatch(1);
        // Ожидание для открытой транзакции
        CountDownLatch releaseFirstTx = new CountDownLatch(1);

        // отправил первый запрос в пул потоков
        Future<?> first = executor.submit(() -> {
            try {
                // Внутри потока вручную открваю транзакцию
                TransactionTemplate tx = new TransactionTemplate(transactionManager);
                tx.execute(status -> {
                    System.out.println("FIRST: before lock");
                    Statement s = statementRepository.findByIdWithLock(businessId)
                            .orElseThrow(() -> new IllegalStateException("Statement not found: " + businessId));

                    System.out.println("FIRST: lock acquired");
                    // Закрываем ожидание
                    firstLocked.countDown();

                    try {
                        // Открываем ожидание с открытой транзакцией
                        releaseFirstTx.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                    s.setCreditId(UUID.randomUUID());
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            return null;
        });

        // Ожидание завершения первой транзакции
        assertTrue(firstLocked.await(2, TimeUnit.SECONDS),
                "Первая транзакция не успела взять lock");
        // Запуск второй залачи во вотром потоке
        Future<?> second = executor.submit(() -> {
            // Также запускаем транзакцию
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
                // Стучимся к тоц же строке, что и в первом потоке, но она залочена так как уде есть транзакция которая её заняла,
                // значит будет жать пока не случится либо коммит, либо ролбек
                Statement s = statementRepository.findByIdWithLock(businessId)
                        .orElseThrow();

                // вставляем данные для проверки того, что к той строке стучались
                s.setCreditId(finalCreditId);
                statementRepository.saveAndFlush(s);
                return null;
            });
            return null;
        });

        // второй вызов должен ждать, пока первая транзакция не завершится, ловим через таймаут
        assertThrows(TimeoutException.class, () -> second.get(300, TimeUnit.MILLISECONDS));

        // освобождаем первую транзакцию
        releaseFirstTx.countDown();

        // Дождался завершения потоков
        first.get(2, TimeUnit.SECONDS);
        second.get(2, TimeUnit.SECONDS);

        Statement actual = statementRepository.findById(businessId)
                .orElseThrow();

        assertEquals(finalCreditId, actual.getCreditId());
    }
}
