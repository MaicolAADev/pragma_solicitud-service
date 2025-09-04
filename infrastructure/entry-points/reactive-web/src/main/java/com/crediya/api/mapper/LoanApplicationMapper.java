package com.crediya.api.mapper;

import com.crediya.api.dto.CreateLoanApplicationDTO;
import com.crediya.api.dto.LoanApplicationDTO;
import com.crediya.model.loanapplication.LoanApplication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {
    LoanApplicationDTO toResponse(LoanApplication loanApplication);
    LoanApplication toModel(CreateLoanApplicationDTO createLoanRequestDTO);
}
