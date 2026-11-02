package com.hotelboutique.bff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProxyController {

    private final RestTemplate restTemplate;

    @Value("${rabbitadmin.service.url}")
    private String rabbitAdminUrl;

    @PostMapping("/queues")
    public ResponseEntity<String> crearCola(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody String body) {
        return reenviar(HttpMethod.POST, "/queues", authHeader, body);
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<String> eliminarCola(@RequestHeader("Authorization") String authHeader,
                                               @PathVariable String name) {
        return reenviar(HttpMethod.DELETE, "/queues/" + codificar(name), authHeader, null);
    }

    @PostMapping("/exchanges")
    public ResponseEntity<String> crearExchange(@RequestHeader("Authorization") String authHeader,
                                                @RequestBody String body) {
        return reenviar(HttpMethod.POST, "/exchanges", authHeader, body);
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<String> eliminarExchange(@RequestHeader("Authorization") String authHeader,
                                                   @PathVariable String name) {
        return reenviar(HttpMethod.DELETE, "/exchanges/" + codificar(name), authHeader, null);
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> crearBinding(@RequestHeader("Authorization") String authHeader,
                                               @RequestBody String body) {
        return reenviar(HttpMethod.POST, "/bindings", authHeader, body);
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<String> eliminarBinding(@RequestHeader("Authorization") String authHeader,
                                                  @RequestParam String exchange,
                                                  @RequestParam String cola,
                                                  @RequestParam String routingKey) {
        String path = UriComponentsBuilder.fromPath("/bindings")
                .queryParam("exchange", exchange)
                .queryParam("cola", cola)
                .queryParam("routingKey", routingKey)
                .build()
                .encode()
                .toUriString();
        return reenviar(HttpMethod.DELETE, path, authHeader, null);
    }

    private String codificar(String segmento) {
        return UriUtils.encodePathSegment(segmento, StandardCharsets.UTF_8);
    }

    private ResponseEntity<String> reenviar(HttpMethod metodo, String path, String authHeader, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        if (body != null) {
            headers.set("Content-Type", "application/json");
        }
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(URI.create(rabbitAdminUrl + path), metodo, entity, String.class);
    }
}
