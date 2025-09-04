package com.crediya.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateLoanApplicationDTO {
    @NotBlank(message = "El monto es obligatorio")
    private BigDecimal amount;
    @NotBlank(message = "El plazo es obligatorio")
    private Integer term;
    @NotBlank(message = "El correo electrónico es obligatorio")
    private String email;
    @NotBlank(message = "El tipo de préstamo es obligatorio")
    private String loanType;
    @NotBlank(message = "El documento de identidad es obligatorio")
    private String identityDocument;
}
