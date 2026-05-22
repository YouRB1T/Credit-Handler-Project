package com.credithandler.deal.service;

import com.credithandler.api.dto.model.StatementDto;

import java.util.List;
import java.util.UUID;

public interface StatementAdminService {

    StatementDto getStatementById(UUID statementId);

    List<StatementDto> getAllStatements();
}
