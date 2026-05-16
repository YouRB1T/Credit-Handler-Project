package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.deal.constants.ErrorConstants;
import com.credithandler.deal.mapper.StatementMapper;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.StatementAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementAdminServiceImpl implements StatementAdminService {

    private final StatementRepository statementRepository;
    private final StatementMapper statementMapper;

    @Override
    public StatementDto getStatementById(UUID statementId) {
        log.info(">> getStatementById, statementId: {}", statementId);

        StatementDto statement = statementRepository.findById(statementId)
                .map(statementMapper::toDto)
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.STATEMENT_NOT_FOUND,
                        String.format(ErrorConstants.STATEMENT_NOT_FOUND_DESC, statementId)
                ));

        log.info("<< getStatementById, statementId: {}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public List<StatementDto> getAllStatements() {
        log.info(">> getAllStatements");

        List<StatementDto> statements = statementRepository.findAll().stream()
                .map(statementMapper::toDto)
                .toList();

        log.info("<< getAllStatements, count: {}", statements.size());
        return statements;
    }
}
