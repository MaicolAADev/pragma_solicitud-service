package com.pragma.solicitudes.infrastructure.web.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponseDto {
    private Long id;
    private String clientDocument;
    private BigDecimal amount;
    private Integer termMonths;
    private String loanType;
    private String status;
    private LocalDateTime applicationDate;
}