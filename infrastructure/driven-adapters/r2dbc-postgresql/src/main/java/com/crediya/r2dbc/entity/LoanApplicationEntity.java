package com.crediya.r2dbc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Data
@Table("solicitud")
public class LoanApplicationEntity {
    @Id
    @Column("id_solicitud")
    private Integer requestId;

    @Column("monto")
    private BigDecimal amount;

    @Column("plazo")
    private Integer term;

    private String email;

    @Column("documento_identidad")
    private String identityDocument;

    @Column("id_estado")
    private Integer stateId;

    @Column("id_tipo_prestamo")
    private Integer loanTypeId;
}
