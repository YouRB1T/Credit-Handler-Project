package com.credithandler.deal.mapper;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.deal.model.Credit;
import com.credithandler.deal.model.enums.CreditStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditMapper {

    @Mapping(target = "creditId", ignore = true)
    @Mapping(target = "insuranceEnabled", source = "isInsuranceEnabled")
    @Mapping(target = "salaryClient", source = "isSalaryClient")
    @Mapping(target = "creditStatus", ignore = true)
    Credit toEntity(CreditDto creditDto);
}
