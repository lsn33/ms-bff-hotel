package com.hotelboutique.bff.messaging;

import com.hotelboutique.bff.event.EventoSolicitud;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudEventPublisher {

    private static final String TIPO = "reserva.solicitada";

    private final RabbitTemplate rabbitTemplate;

    @Value("${hotel.messaging.exchanges.eventos}")
    private String exchangeEventos;

    public void publicarSolicitud(String usuarioEmail) {
        EventoSolicitud evento = new EventoSolicitud(TIPO, null, usuarioEmail, null, null);
        try {
            rabbitTemplate.convertAndSend(exchangeEventos, TIPO, evento);
            log.info("Solicitud de reserva publicada para {}", usuarioEmail);
        } catch (AmqpException ex) {
            log.error("No se pudo publicar la solicitud de reserva de {}: {}",
                    usuarioEmail, ex.getMessage(), ex);
        }
    }
}
