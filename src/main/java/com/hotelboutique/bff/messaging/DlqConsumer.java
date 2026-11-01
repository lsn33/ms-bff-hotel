package com.hotelboutique.bff.messaging;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DlqConsumer {

    @RabbitListener(queues = "${hotel.messaging.dlq.notificaciones}")
    public void onNotificacionesDlq(Message mensaje, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        registrar("q.notificaciones.dlq", mensaje, channel, tag);
    }

    @RabbitListener(queues = "${hotel.messaging.dlq.tickets}")
    public void onTicketsDlq(Message mensaje, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        registrar("q.tickets.dlq", mensaje, channel, tag);
    }

    @RabbitListener(queues = "${hotel.messaging.dlq.documentos}")
    public void onDocumentosDlq(Message mensaje, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        registrar("q.documentos.dlq", mensaje, channel, tag);
    }

    private void registrar(String cola, Message mensaje, Channel channel, long tag) throws IOException {
        Object motivo = mensaje.getMessageProperties().getHeader("x-death");
        String payload = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        log.error("Mensaje no entregado en {} | motivo(x-death)={} | payload={}", cola, motivo, payload);
        channel.basicAck(tag, false);
    }
}
