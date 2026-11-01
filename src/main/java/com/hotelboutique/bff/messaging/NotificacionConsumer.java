package com.hotelboutique.bff.messaging;

import com.hotelboutique.bff.event.EventoSolicitud;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class NotificacionConsumer {

    @RabbitListener(queues = "${hotel.messaging.queues.notificaciones}")
    public void onNotificacion(EventoSolicitud evento, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        if (evento == null || evento.tipo() == null || evento.tipo().isBlank()) {
            log.error("Notificacion invalida, NACK sin requeue");
            channel.basicNack(tag, false, false);
            return;
        }

        log.info("Notificacion [{}] para {}", evento.tipo(), evento.usuarioEmail());
        channel.basicAck(tag, false);
    }
}
