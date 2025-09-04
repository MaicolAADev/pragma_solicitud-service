package com.crediya.r2dbc.loanapplication;

import com.crediya.r2dbc.entity.LoanApplicationEntity;
import io.micrometer.common.lang.Nullable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LoanApplicationReactiveRepository extends
        ReactiveCrudRepository<LoanApplicationEntity, String>,
        ReactiveQueryByExampleExecutor<LoanApplicationEntity> {
    @Query("""
            SELECT s.*
            FROM solicitud s
            INNER JOIN estados e ON e.id_estado = s.id_estado
            INNER JOIN tipo_prestamo tp ON tp.id_tipo_prestamo = s.id_tipo_prestamo
            WHERE e.id_estado <> 2
              AND (:email IS NULL OR s.email = :email)
              AND (:loanType IS NULL OR tp.nombre = :loanType)
              AND (:status IS NULL OR e.nombre = :status)
            LIMIT :size OFFSET :offset
            """)
    Flux<LoanApplicationEntity> findAllFiltered(
            @Param("offset") int offset,
            @Param("size") int size,
            @Nullable @Param("email") String email,
            @Nullable @Param("loanType") String loanType,
            @Nullable @Param("status") String status
    );

    @Query("SELECT * FROM solicitud WHERE documento_identidad = :identityDocument AND id_estado = 2")
    Flux<LoanApplicationEntity> findApprovedByIdentity(String identityDocument);
}
