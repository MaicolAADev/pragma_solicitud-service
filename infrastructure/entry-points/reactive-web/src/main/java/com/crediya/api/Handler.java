package com.crediya.api;

import com.crediya.api.dto.CreateLoanApplicationDTO;
import com.crediya.api.mapper.LoanApplicationMapper;
import com.crediya.api.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {
    private final LoanApplicationService loanApplicationService;
    private final LoanApplicationMapper loanApplicationMapper;

    public Mono<ServerResponse> listenSaveLoanApplication(ServerRequest serverRequest) {
        log.debug("Recibiendo petición para crear solicitud de préstamo");
        String token = serverRequest.headers().firstHeader("Authorization");
        return serverRequest.bodyToMono(CreateLoanApplicationDTO.class)
                .doOnNext(dto -> log.debug("Payload recibido: {}", dto))
                .map(loanApplicationMapper::toModel)
                .flatMap(loanApplication -> loanApplicationService.saveLoanRequest(loanApplication, token))
                .map(loanApplicationMapper::toResponse)
                .flatMap(loanApplicationResponse -> ServerResponse.ok().bodyValue(loanApplicationResponse));
    }

    public Mono<ServerResponse> listenListApplicationsForReview(ServerRequest serverRequest) {
        log.debug("Recibiendo petición para listar solicitudes de préstamo en revisión");
        String token = serverRequest.headers().firstHeader("Authorization");
        int page = Integer.parseInt(serverRequest.queryParam("page").orElse("0"));
        int size = Integer.parseInt(serverRequest.queryParam("size").orElse("10"));
        String email = serverRequest.queryParam("email").orElse(null);
        String loanType = serverRequest.queryParam("tipoPrestamo").orElse(null);
        String status = serverRequest.queryParam("estado").orElse(null);

        return loanApplicationService.listApplicationsForReview(page, size, token, email, loanType, status)
                .flatMap(applications -> ServerResponse.ok().bodyValue(applications));
    }
}
