package com.crediya.r2dbc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("estados")
public class StateEntity {
    @Id
    @Column("id_estado")
    private Integer stateId;
    @Column("nombre")
    private String name;
    @Column("descripcion")
    private String description;
}

