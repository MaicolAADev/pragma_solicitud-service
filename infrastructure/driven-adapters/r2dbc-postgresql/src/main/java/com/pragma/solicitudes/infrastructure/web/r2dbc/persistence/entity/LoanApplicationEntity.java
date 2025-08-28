package com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("solicitud_prestamo")
public class LoanApplicationEntity {
    @Id
    @Column("id")
    private Long id;

    @Column("cliente_documento")
    private String clientDocument;

    @Column("monto")
    private BigDecimal amount;

    @Column("plazo_meses")
    private Integer termMonths;

    @Column("tipo_prestamo")
    private String loanType;

    @Column("estado")
    private String status;

    @Column("fecha_solicitud")
    private LocalDateTime applicationDate;
}