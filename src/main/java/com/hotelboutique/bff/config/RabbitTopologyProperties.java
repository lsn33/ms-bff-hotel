package com.hotelboutique.bff.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "hotel.messaging")
public record RabbitTopologyProperties(
        @NotBlank String dlx,
        @NotNull Exchanges exchanges,
        @NotNull Queues queues,
        @NotNull Routing routing,
        @NotNull Retencion retencion
) {

    public record Exchanges(@NotBlank String eventos, @NotBlank String documentos) {
    }

    public record Queues(@NotBlank String notificaciones,
                         @NotBlank String tickets,
                         @NotBlank String documentos) {
    }

    public record Routing(@NotNull List<String> notificaciones,
                          @NotNull List<String> tickets,
                          @NotNull List<String> documentos) {
    }

    public record Retencion(@Positive int maxLength, @Positive long ttlMs) {
    }
}
