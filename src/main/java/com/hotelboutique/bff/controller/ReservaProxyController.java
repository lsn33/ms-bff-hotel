package com.hotelboutique.bff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaProxyController {

    private final RestTemplate restTemplate;

    @Value("${reservas.service.url}")
    private String reservasServiceUrl;

    @PostMapping
    public ResponseEntity<String> crear(@RequestHeader("Authorization") String authHeader, @RequestBody String body) {
        return reenviar(HttpMethod.POST, "/reservas", authHeader, body);
    }

    @GetMapping("/mias")
    public ResponseEntity<String> misReservas(@RequestHeader("Authorization") String authHeader) {
        return reenviar(HttpMethod.GET, "/reservas/mias", authHeader, null);
    }

    @GetMapping
    public ResponseEntity<String> listarTodas(@RequestHeader("Authorization") String authHeader) {
        return reenviar(HttpMethod.GET, "/reservas", authHeader, null);
    }

    @PutMapping("/{id}/checkin")
    public ResponseEntity<String> checkin(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return reenviar(HttpMethod.PUT, "/reservas/" + id + "/checkin", authHeader, null);
    }

    @PutMapping("/{id}/checkout")
    public ResponseEntity<String> checkout(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return reenviar(HttpMethod.PUT, "/reservas/" + id + "/checkout", authHeader, null);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelar(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return reenviar(HttpMethod.PUT, "/reservas/" + id + "/cancelar", authHeader, null);
    }

    private ResponseEntity<String> reenviar(HttpMethod metodo, String path, String authHeader, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        if (body != null) headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(reservasServiceUrl + path, metodo, entity, String.class);
    }
}