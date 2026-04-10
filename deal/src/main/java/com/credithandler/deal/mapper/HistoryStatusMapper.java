package com.credithandler.deal.mapper;

import com.credithandler.api.dto.model.HistoryStatusDto;
import com.credithandler.deal.model.HistoryStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryStatusMapper {

    HistoryStatusDto toDto(HistoryStatus entity);

}
