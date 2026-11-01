package com.hotelboutique.bff.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

class DlqConsumerTest {

    private Message mensaje(String cuerpo) {
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }

    @Test
    void registraYConfirmaMensajeDeNotificacionesDlq() throws Exception {
        Channel channel = mock(Channel.class);

        new DlqConsumer().onNotificacionesDlq(mensaje("{\"tipo\":\"reserva.solicitada\"}"), channel, 4L);

        verify(channel).basicAck(4L, false);
    }

    @Test
    void registraYConfirmaMensajeDeTicketsDlq() throws Exception {
        Channel channel = mock(Channel.class);

        new DlqConsumer().onTicketsDlq(mensaje("{\"tipo\":\"reserva.creada\"}"), channel, 5L);

        verify(channel).basicAck(5L, false);
    }

    @Test
    void registraYConfirmaMensajeDeDocumentosDlq() throws Exception {
        Channel channel = mock(Channel.class);

        new DlqConsumer().onDocumentosDlq(mensaje("{\"tipo\":\"reserva.checkout\"}"), channel, 6L);

        verify(channel).basicAck(6L, false);
    }
}
