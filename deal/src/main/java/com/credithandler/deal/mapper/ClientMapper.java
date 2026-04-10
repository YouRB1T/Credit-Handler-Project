package com.credithandler.deal.mapper;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "passport", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "maritalStatus", ignore = true)
    @Mapping(target = "dependentAmount", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "employmentId", ignore = true)
    Client toEntity(LoanStatementRequestDto dto);
}
