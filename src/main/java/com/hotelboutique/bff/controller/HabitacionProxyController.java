package com.hotelboutique.bff.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/habitaciones")
public class HabitacionProxyController {

    @Autowired // Corregido: Inyección directa nativa de Spring sin depender de Lombok
    private RestTemplate restTemplate;

    @Value("${reservas.service.url}")
    private String reservasServiceUrl;

    @GetMapping("/disponibles")
    public ResponseEntity<String> listarDisponibles() {
        return restTemplate.getForEntity(reservasServiceUrl + "/habitaciones/disponibles", String.class);
    }

    @GetMapping
    public ResponseEntity<String> listarTodas(@RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                reservasServiceUrl + "/habitaciones", HttpMethod.GET, entity, String.class
        );
    }

    @PostMapping
    public ResponseEntity<String> crear(@RequestHeader("Authorization") String authHeader, @RequestBody String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                reservasServiceUrl + "/habitaciones", HttpMethod.POST, entity, String.class
        );
    }
}
