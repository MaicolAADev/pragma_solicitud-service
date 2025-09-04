package com.crediya.api.service;

import com.crediya.api.config.ApiResponse;
import com.crediya.api.dto.IdentitiesRequestDTO;
import com.crediya.api.dto.LoanApplicationWithUserDTO;
import com.crediya.api.dto.TokenInfoResponse;
import com.crediya.api.dto.UserDTO;
import com.crediya.model.loanapplication.LoanApplication;
import com.crediya.model.loanapplication.gateways.LoanApplicationInputPort;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
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
                    // Verificar que el rol sea "User"
                    if (!"User".equals(tokenInfo.role())) {
                        return Mono.error(new RuntimeException("Solo usuarios con rol 'User' pueden crear solicitudes de préstamo"));
                    }

                    // Verificar que el usuario solo pueda crear solicitudes para sí mismo
                    if (!tokenInfo.identityDocument().equals(loanApplication.getIdentityDocument())) {
                        return Mono.error(new RuntimeException("Solo puedes crear solicitudes de préstamo para tu propio documento de identidad"));
                    }

                    // Si todo está bien, proceder con la creación
                    String url = authBaseUrl + getUserEndpoint + loanApplication.getEmail();

                    return webClient.get()
                            .uri(url)
                            .header("Authorization", token)
                            .header("Content-Type", "application/json")
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError(), response ->
                                    Mono.error(new RuntimeException("Error al buscar usuario: " + response.statusCode()))
                            )
                            .onStatus(status -> status.is5xxServerError(), response ->
                                    Mono.error(new RuntimeException("Error interno del servidor: " + response.statusCode()))
                            )
                            .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {})
                            .doOnNext(response -> log.info("Usuario encontrado: {}", response.getContent()))
                            .flatMap(response -> loanApplicationInputPort.save(loanApplication))
                            .onErrorResume(WebClientResponseException.class, error -> {
                                log.error("Error al buscar usuario: {}", error.getMessage());
                                return Mono.error(new RuntimeException("No se pudo registrar la solicitud, el usuario no fue encontrado"));
                            })
                            .onErrorResume(error -> {
                                log.error("Fallo al obtener usuario: {}", error.getMessage());
                                return Mono.error(new RuntimeException("No se pudo registrar la solicitud: " + error.getMessage()));
                            });
                });
    }

    public Mono<List<LoanApplicationWithUserDTO>> listApplicationsForReview(
            int page, int size, String token, @Nullable String email, @Nullable String loanType, @Nullable String status) {
        log.info("Listando solicitudes en revisión - página {}, tamaño {}", page, size);

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
                        Mono.error(new RuntimeException("Error de cliente al obtener usuarios: " + response.statusCode()))
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        Mono.error(new RuntimeException("Error interno al obtener usuarios: " + response.statusCode()))
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

    private Mono<TokenInfoResponse> validateTokenAndGetUserInfo(String token) {
        String validateTokenUrl = authBaseUrl + "/api/v1/validate-token";

        return webClient.get()
                .uri(validateTokenUrl)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        Mono.error(new RuntimeException("Token inválido: " + response.statusCode()))
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        Mono.error(new RuntimeException("Error interno al validar token: " + response.statusCode()))
                )
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<TokenInfoResponse>>() {})
                .map(ApiResponse::getContent)
                .flatMap(tokenInfo -> {
                    if (!tokenInfo.valid()) {
                        return Mono.error(new RuntimeException("Token inválido o expirado"));
                    }
                    return Mono.just(tokenInfo);
                })
                .onErrorResume(WebClientResponseException.Unauthorized.class, error -> {
                    log.error("Token no autorizado: {}", error.getMessage());
                    return Mono.error(new RuntimeException("Token no autorizado"));
                })
                .onErrorResume(WebClientResponseException.Forbidden.class, error -> {
                    log.error("Acceso prohibido: {}", error.getMessage());
                    return Mono.error(new RuntimeException("Acceso prohibido"));
                })
                .onErrorResume(error -> {
                    log.error("Error al validar token: {}", error.getMessage());
                    return Mono.error(new RuntimeException("Error de autenticación: " + error.getMessage()));
                });
    }


}