package com.crediya.api;

import com.crediya.api.dto.CreateLoanApplicationDTO;
import com.crediya.api.mapper.LoanApplicationMapper;
import com.crediya.api.service.LoanApplicationService;
import com.crediya.usecase.exception.ArgumentException;
import com.crediya.usecase.exception.ForbbidenException;
import com.crediya.usecase.exception.UnhautorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                .flatMap(loanApplicationResponse -> ServerResponse.ok().bodyValue(loanApplicationResponse))
                .onErrorResume(error -> handleException(error));
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
                .flatMap(applications -> ServerResponse.ok().bodyValue(applications))
                .onErrorResume(error -> {
                    log.error("Error en listenListApplicationsForReview: {}", error.getMessage());
                   return handleException(error);
                });
    }

    public Mono<ServerResponse> listenUpdateLoanApplication(ServerRequest serverRequest) {
        log.debug("Recibiendo petición para actualizar solicitud de préstamo");
        String token = serverRequest.headers().firstHeader("Authorization");
        String id = serverRequest.pathVariable("id");
        String action = serverRequest.pathVariable("action");
        return loanApplicationService.updateLoanRequest(id, action, token)
                .flatMap(loanApplication -> ServerResponse.ok().bodyValue(loanApplication))
                .onErrorResume(error -> handleException(error));
    }

    private Mono<ServerResponse> handleException(Throwable error) {
        if (error instanceof ArgumentException) {
            log.warn("Parámetro inválido: {}", error.getMessage());
            return ServerResponse.badRequest()
                    .bodyValue(Map.of(
                            "error", "INVALID_PARAMS",
                            "errors", new String[]{error.getMessage()}
                    ));
        } else if (error instanceof ForbbidenException) {
            log.warn("Acceso denegado: {}", error.getMessage());
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .bodyValue(Map.of(
                            "error", "FORBIDDEN",
                            "errors", new String[]{error.getMessage()}
                    ));
        } else if (error instanceof UnhautorizedException) {
            log.warn("Acceso no autorizado: {}", error.getMessage());
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .bodyValue(Map.of(
                            "error", "UNAUTHORIZED",
                            "errors", new String[]{error.getMessage()}
                    ));
        } else {
            log.error("Error inesperado: {}", error.getMessage(), error);

            if (error.getMessage() != null) {
                Pattern pattern = Pattern.compile("\"status\":401");
                Matcher matcher = pattern.matcher(error.getMessage());
                if (matcher.find()) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                            .bodyValue(Map.of(
                                    "error", "UNAUTHORIZED",
                                    "errors", new String[]{error.getMessage()}
                            ));
                }
            }

            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue(Map.of(
                            "error", "INTERNAL_SERVER_ERROR",
                            "errors", new String[]{error.getMessage()}
                    ));
        }
    }
}
