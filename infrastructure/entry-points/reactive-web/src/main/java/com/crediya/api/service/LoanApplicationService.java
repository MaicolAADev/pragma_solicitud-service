package com.crediya.api.service;

import com.crediya.api.dto.IdentitiesRequestDTO;
import com.crediya.api.dto.LoanApplicationWithUserDTO;
import com.crediya.api.dto.TokenInfoResponse;
import com.crediya.api.dto.UserDTO;
import com.crediya.model.loanapplication.LoanApplication;
import com.crediya.model.loanapplication.gateways.LoanApplicationInputPort;
import com.crediya.usecase.exception.ArgumentException;
import com.crediya.usecase.exception.ForbbidenException;
import com.crediya.usecase.exception.UnhautorizedException;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {
    private final LoanApplicationInputPort loanApplicationInputPort;
    private final WebClient webClient;

    @Value("${services.auth.base-url}")
    private String authBaseUrl;

    @Value("${services.auth.endpoints.get-user}")
    private String getUserEndpoint;

    public Mono<LoanApplication> saveLoanRequest(LoanApplication loanApplication, String token) {
        return validateTokenAndGetUserInfo(token)
                .flatMap(tokenInfo -> {
                    if (!"User".equals(tokenInfo.role())) {
                        return Mono.error(new ForbbidenException("Solo usuarios con rol 'User' pueden crear solicitudes de préstamo"));
                    }

                    if (!tokenInfo.identityDocument().equals(loanApplication.getIdentityDocument())) {
                        return Mono.error(new ArgumentException("Solo puedes crear solicitudes de préstamo para tu propio documento de identidad"));
                    }

                    String url = authBaseUrl + getUserEndpoint + loanApplication.getEmail();

                    return webClient.get()
                            .uri(url)
                            .header("Authorization", token)
                            .header("Content-Type", "application/json")
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError(), response ->
                                    Mono.error(new UnhautorizedException("No tiene autorizacion para realizar esta accion : " + response.statusCode()))
                            )
                            .onStatus(status -> status.is5xxServerError(), response ->
                                    Mono.error(new RuntimeException("Error interno del servidor: " + response.statusCode()))
                            )
                            .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {})
                            .doOnNext(response -> log.info("Usuario encontrado: {}", response.getContent()))
                            .flatMap(response -> loanApplicationInputPort.save(loanApplication))
                            .onErrorResume(WebClientResponseException.class, error -> {
                                log.error("Error al buscar usuario: {}", error.getMessage());
                                return Mono.error(new ArgumentException("No se pudo registrar la solicitud, el usuario no fue encontrado"));
                            })
                            .onErrorResume(error -> {
                                log.error("Fallo al obtener usuario: {}", error.getMessage());
                                return Mono.error(new ArgumentException("No se pudo registrar la solicitud: " + error.getMessage()));
                            });
                });
    }

    public Mono<List<LoanApplicationWithUserDTO>> listApplicationsForReview(
            int page, int size, String token, @Nullable String email, @Nullable String loanType, @Nullable String status) {
        log.info("Listando solicitudes en revisión - página {}, tamaño {}", page, size);

        return validateTokenAndGetUserInfo(token)
                .flatMap(tokenInfo -> {
                    if (!"Adviser".equals(tokenInfo.role())) {
                        log.warn("Usuario con rol {} intentó acceder a listApplicationsForReview", tokenInfo.role());
                        return Mono.error(new ForbbidenException("Solo usuarios con rol 'Adviser' pueden listar solicitudes en revisión"));
                    }

                    log.debug("Usuario Adviser autenticado: {}", tokenInfo.email());

                    return loanApplicationInputPort.findByStates(page, size, email, loanType, status)
                            .flatMap(applications -> {
                                List<String> identityDocs = applications.stream()
                                        .map(app -> app.getBase().getIdentityDocument())
                                        .toList();

                                return fetchUsers(identityDocs, token)
                                        .map(users -> applications.stream()
                                                .map(app -> {
                                                    UserDTO user = users.stream()
                                                            .filter(u -> u.identityDocument().equals(app.getBase().getIdentityDocument()))
                                                            .findFirst()
                                                            .orElse(null);
                                                    return LoanApplicationWithUserDTO.of(app, user);
                                                })
                                                .toList()
                                        );
                            });
                })
                .onErrorResume(error -> {
                    if(!(error instanceof ForbbidenException) && !(error instanceof UnhautorizedException))
                    {
                        log.error("Error en listApplicationsForReview: {}", error.getMessage());
                        return Mono.error(new RuntimeException("No se pudieron listar las solicitudes: " + error.getMessage()));
                    }
                    return Mono.error(error);
                });
    }

    private Mono<List<UserDTO>> fetchUsers(List<String> identityDocs, String token) {
        String url = authBaseUrl + getUserEndpoint + "identification-numbers";
        IdentitiesRequestDTO requestBody = new IdentitiesRequestDTO();

        return webClient.post()
                .uri(url)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        Mono.error(new ArgumentException("Error de cliente al obtener usuarios: " + response.statusCode()))
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        Mono.error(new ArgumentException("Error interno al obtener usuarios: " + response.statusCode()))
                )
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<UserDTO>>>() {})
                .map(ApiResponse::getContent)
                .onErrorResume(WebClientResponseException.class, error -> {
                    log.error("Error al obtener usuarios: {}", error.getMessage());
                    return Mono.just(List.of());
                })
                .onErrorResume(error -> {
                    log.error("Error al obtener usuarios, verifique los documentos de identidad asociados a las solicitudes: {}", error.getMessage());
                    return Mono.just(List.of());
                });
    }

    public Mono<LoanApplication> updateLoanRequest(String id, String action, String token) {
        log.info("Actualizando solicitud de préstamo con ID: {}", id);
        log.info("Acción: {}", action);

        return validateTokenAndGetUserInfo(token)
                .flatMap(tokenInfo -> {
                    if (!"Adviser".equals(tokenInfo.role())) {
                        log.warn("Solo los usuarios con rol Adviser pueden aprobar o rechazar solicitudes", tokenInfo.role());
                        return Mono.error(new ForbbidenException("Solo los usuarios con rol Adviser pueden aprobar o rechazar solicitudes"));
                    }

                    log.debug("Usuario Adviser autenticado: {}", tokenInfo.email());

                    return loanApplicationInputPort.updateLoanRequest(id, action, tokenInfo.email());
                })
                .onErrorResume(error -> {
                    if(!(error instanceof ForbbidenException) && !(error instanceof UnhautorizedException))
                    {
                        log.error("Error en updateLoanRequest: {}", error.getMessage());
                        return Mono.error(new RuntimeException("No se pudo actualizar la solicitud: " + error.getMessage()));
                    }
                    return Mono.error(error);
                });

    }

    private Mono<TokenInfoResponse> validateTokenAndGetUserInfo(String token) {
        String validateTokenUrl = authBaseUrl + "/api/v1/validate-token";
        log.debug("Validando token en URL: {}", validateTokenUrl);

        return webClient.get()
                .uri(validateTokenUrl)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Error 4xx al validar token: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new UnhautorizedException("Token inválido: " + response.statusCode() + " - " + errorBody)));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Error 5xx al validar token: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new ArgumentException("Error interno: " + response.statusCode() + " - " + errorBody)));
                })
                .bodyToMono(TokenInfoResponse.class)
                .doOnNext(tokenInfo -> log.debug("Token info recibido: {}", tokenInfo))
                .flatMap(tokenInfo -> {
                    if (!tokenInfo.valid()) {
                        log.warn("Token marcado como inválido: {}", tokenInfo);
                        return Mono.error(new UnhautorizedException("Token inválido o expirado"));
                    }
                    log.debug("Token válido: {}", tokenInfo);
                    return Mono.just(tokenInfo);
                })
                .onErrorResume(WebClientResponseException.Unauthorized.class, error -> {
                    log.error("Token no autorizado: {}", error.getResponseBodyAsString());
                    return Mono.error(new UnhautorizedException("Token no autorizado: " + error.getResponseBodyAsString()));
                })
                .onErrorResume(WebClientResponseException.Forbidden.class, error -> {
                    log.error("Acceso prohibido: {}", error.getResponseBodyAsString());
                    return Mono.error(new ForbbidenException("Acceso prohibido: " + error.getResponseBodyAsString()));
                })
                .onErrorResume(error -> {
                    log.error("Error al validar token: {}", error.getMessage());
                    return Mono.error(new UnhautorizedException("Error de autenticación: " + error.getMessage()));
                });
    }

    public static class ApiResponse<T> {
        private T content;
        private String message;
        private boolean success;

        public T getContent() { return content; }
        public void setContent(T content) { this.content = content; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}