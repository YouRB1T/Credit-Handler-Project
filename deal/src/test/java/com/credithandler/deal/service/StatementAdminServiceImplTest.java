package com.credithandler.deal.service;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.deal.constants.ErrorConstants;
import com.credithandler.deal.mapper.StatementMapper;
import com.credithandler.deal.model.Statement;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.impl.StatementAdminServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@DisplayName("Тестирование админского сервиса заявок")

class StatementAdminServiceImplTest {

    private final StatementRepository statementRepository = mock(StatementRepository.class);
    private final StatementMapper statementMapper = mock(StatementMapper.class);
    private final StatementAdminService service = new StatementAdminServiceImpl(statementRepository, statementMapper);

    @Test
    @DisplayName("getStatementById возвращает смапленную заявку")
    void getStatementByIdReturnsMappedStatement() {
        UUID statementId = UUID.randomUUID();
        Statement statement = new Statement();
        StatementDto statementDto = new StatementDto();
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(statementMapper.toDto(statement)).thenReturn(statementDto);

        StatementDto result = service.getStatementById(statementId);

        assertThat(result).isSameAs(statementDto);
        verify(statementRepository).findById(statementId);
        verify(statementMapper).toDto(statement);
    }

    @Test
    @DisplayName("getStatementById выбрасывает BusinessException если заявка не найдена")
    void getStatementByIdThrowsBusinessExceptionWhenStatementDoesNotExist() {
        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatementById(statementId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.STATEMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("getAllStatements возвращает смапленные заявки")
    void getAllStatementsReturnsMappedStatements() {
        Statement firstStatement = new Statement();
        Statement secondStatement = new Statement();
        StatementDto firstStatementDto = new StatementDto();
        StatementDto secondStatementDto = new StatementDto();
        when(statementRepository.findAll()).thenReturn(List.of(firstStatement, secondStatement));
        when(statementMapper.toDto(firstStatement)).thenReturn(firstStatementDto);
        when(statementMapper.toDto(secondStatement)).thenReturn(secondStatementDto);

        List<StatementDto> result = service.getAllStatements();

        assertThat(result).containsExactly(firstStatementDto, secondStatementDto);
        verify(statementRepository).findAll();
    }
}
