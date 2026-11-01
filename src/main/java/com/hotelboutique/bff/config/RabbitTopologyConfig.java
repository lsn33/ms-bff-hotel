package com.hotelboutique.bff.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(RabbitTopologyProperties.class)
public class RabbitTopologyConfig {

    private static final String SUFIJO_DLQ = ".dlq";

    private final RabbitTopologyProperties props;

    public RabbitTopologyConfig(RabbitTopologyProperties props) {
        this.props = props;
    }

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(props.exchanges().eventos(), true, false);
    }

    @Bean
    public DirectExchange documentosExchange() {
        return new DirectExchange(props.exchanges().documentos(), true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(props.dlx(), true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return colaConDlq(props.queues().notificaciones());
    }

    @Bean
    public Queue ticketsQueue() {
        return colaConDlq(props.queues().tickets());
    }

    @Bean
    public Queue documentosQueue() {
        return colaConDlq(props.queues().documentos());
    }

    @Bean
    public Queue notificacionesDlq() {
        return dlq(props.queues().notificaciones());
    }

    @Bean
    public Queue ticketsDlq() {
        return dlq(props.queues().tickets());
    }

    @Bean
    public Queue documentosDlq() {
        return dlq(props.queues().documentos());
    }

    @Bean
    public Declarables bindings() {
        List<Declarable> enlaces = new ArrayList<>();

        for (String rk : props.routing().notificaciones()) {
            enlaces.add(BindingBuilder.bind(notificacionesQueue()).to(eventosExchange()).with(rk));
        }
        for (String rk : props.routing().tickets()) {
            enlaces.add(BindingBuilder.bind(ticketsQueue()).to(eventosExchange()).with(rk));
        }
        for (String rk : props.routing().documentos()) {
            enlaces.add(BindingBuilder.bind(documentosQueue()).to(documentosExchange()).with(rk));
        }

        enlaces.add(BindingBuilder.bind(notificacionesDlq()).to(dlxExchange())
                .with(props.queues().notificaciones() + SUFIJO_DLQ));
        enlaces.add(BindingBuilder.bind(ticketsDlq()).to(dlxExchange())
                .with(props.queues().tickets() + SUFIJO_DLQ));
        enlaces.add(BindingBuilder.bind(documentosDlq()).to(dlxExchange())
                .with(props.queues().documentos() + SUFIJO_DLQ));

        return new Declarables(enlaces);
    }

    private Queue colaConDlq(String nombre) {
        return QueueBuilder.durable(nombre)
                .withArgument("x-dead-letter-exchange", props.dlx())
                .withArgument("x-dead-letter-routing-key", nombre + SUFIJO_DLQ)
                .withArgument("x-max-length", props.retencion().maxLength())
                .withArgument("x-message-ttl", props.retencion().ttlMs())
                .build();
    }

    private Queue dlq(String nombreColaOriginal) {
        return QueueBuilder.durable(nombreColaOriginal + SUFIJO_DLQ).build();
    }
}
