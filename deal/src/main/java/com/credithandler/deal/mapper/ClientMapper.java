package com.credithandler.deal.mapper;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// TODO: Написать тесты на Mapper
@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "clientId", ignore = true)
    Client toEntity(LoanStatementRequestDto dto);
}
