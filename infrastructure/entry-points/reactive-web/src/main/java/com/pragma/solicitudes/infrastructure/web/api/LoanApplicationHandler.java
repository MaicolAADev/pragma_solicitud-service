package com.pragma.solicitudes.infrastructure.web.api;

import com.pragma.solicitudes.domain.usecase.LoanApplicationRequest;
import com.pragma.solicitudes.domain.usecase.LoanApplicationUseCase;
import com.pragma.solicitudes.infrastructure.web.dto.LoanApplicationRequestDto;
import com.pragma.solicitudes.infrastructure.web.dto.LoanApplicationResponseDto;
import com.pragma.solicitudes.infrastructure.web.mapper.LoanApplicationWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationHandler {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationWebMapper mapper;

    public Mono<ServerResponse> createLoanApplication(ServerRequest request) {
        return request.bodyToMono(LoanApplicationRequestDto.class)
                .doOnNext(dto -> log.info("Received loan application request for client: {}", dto.getClientDocument()))
                .flatMap(this::processLoanApplication)
                .flatMap(responseDto -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .onErrorResume(this::handleError);
    }

    private Mono<LoanApplicationResponseDto> processLoanApplication(LoanApplicationRequestDto dto) {
        LoanApplicationRequest domainRequest = mapper.toDomainRequest(dto);
        return loanApplicationUseCase.processLoanApplication(domainRequest)
                .map(mapper::toResponseDto)
                .doOnSuccess(response -> log.info("Loan application created successfully. ID: {}", response.getId()))
                .doOnError(error -> log.error("Error processing loan application: {}", error.getMessage()));
    }

    private Mono<ServerResponse> handleError(Throwable error) {
        log.error("Error in loan application processing: {}", error.getMessage(), error);

        if (error instanceof IllegalArgumentException) {
            return ServerResponse.badRequest()
                    .bodyValue(new ErrorResponse("VALIDATION_ERROR", new ArrayList<>(List.of(error.getMessage()))));
        }

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue(new ErrorResponse("INTERNAL_SERVER_ERROR", new ArrayList<>(List.of("Error interno del servidor"))));
    }

    private record ErrorResponse(String error, ArrayList<String> errors) {}

}