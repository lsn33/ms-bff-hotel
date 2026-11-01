package com.hotelboutique.bff.messaging;

import com.hotelboutique.bff.event.EventoSolicitud;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NotificacionConsumerTest {

    @Test
    void registraNotificacionYConfirmaConAck() throws Exception {
        Channel channel = mock(Channel.class);
        EventoSolicitud evento = new EventoSolicitud("reserva.solicitada", null, "nuevo@hotel.cl", null, null);

        new NotificacionConsumer().onNotificacion(evento, channel, 2L);

        verify(channel).basicAck(2L, false);
    }

    @Test
    void tipoVacioHaceNackSinRequeue() throws Exception {
        Channel channel = mock(Channel.class);
        EventoSolicitud evento = new EventoSolicitud(" ", null, "nuevo@hotel.cl", null, null);

        new NotificacionConsumer().onNotificacion(evento, channel, 5L);

        verify(channel).basicNack(5L, false, false);
    }
}
