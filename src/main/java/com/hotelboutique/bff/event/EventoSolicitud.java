package com.hotelboutique.bff.event;

import java.time.LocalDate;

public record EventoSolicitud(
        String tipo,
        Long reservaId,
        String usuarioEmail,
        LocalDate fechaCheckin,
        LocalDate fechaCheckout
) {
}
