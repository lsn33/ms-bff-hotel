package com.hotelboutique.bff.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProxyControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AdminProxyController controller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "rabbitAdminUrl", "http://localhost:8090");
    }

    @Test
    void crearColaReenviaPostConElToken() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.status(201).build());

        ResponseEntity<String> respuesta = controller.crearCola("******", "{\"nombre\":\"q.x\",\"durable\":true}");

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        ArgumentCaptor<HttpEntity> entidad = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(URI.create("http://localhost:8090/queues")),
                eq(HttpMethod.POST), entidad.capture(), eq(String.class));
        assertThat(entidad.getValue().getHeaders().getFirst("Authorization")).isEqualTo("******");
    }

    @Test
    void eliminarBindingCodificaLaRoutingKeyConNumeral() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.noContent().build());

        controller.eliminarBinding("******", "hotel.eventos", "q.tickets", "reserva.#");

        verify(restTemplate).exchange(
                eq(URI.create("http://localhost:8090/bindings?exchange=hotel.eventos&cola=q.tickets&routingKey=reserva.%23")),
                eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void eliminarColaCodificaElNombre() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.noContent().build());

        controller.eliminarCola("******", "q.prueba");

        verify(restTemplate).exchange(eq(URI.create("http://localhost:8090/queues/q.prueba")),
                eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class));
    }
}
