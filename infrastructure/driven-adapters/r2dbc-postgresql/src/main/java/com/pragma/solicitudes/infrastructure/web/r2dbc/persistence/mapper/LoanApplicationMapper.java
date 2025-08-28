package com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.mapper;

import com.pragma.solicitudes.domain.model.LoanApplication;
import com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.entity.LoanApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "clientDocument", source = "clientDocument")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "termMonths", source = "termMonths")
    @Mapping(target = "loanType", source = "loanType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "applicationDate", source = "applicationDate")
    LoanApplicationEntity toEntity(LoanApplication loanApplication);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "clientDocument", source = "clientDocument")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "termMonths", source = "termMonths")
    @Mapping(target = "loanType", source = "loanType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "applicationDate", source = "applicationDate")
    LoanApplication toDomain(LoanApplicationEntity entity);
}