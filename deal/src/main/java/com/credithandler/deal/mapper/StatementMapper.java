package com.credithandler.deal.mapper;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.model.Statement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ClientMapper.class})
public interface StatementMapper {

    @Mapping(target = "statementId", ignore = true)
    @Mapping(target = "clientId", source = "client.clientId")
    @Mapping(target = "appliedOffers", ignore = true)
    Statement toEntity(LoanStatementRequestDto dto, Client client);
}
