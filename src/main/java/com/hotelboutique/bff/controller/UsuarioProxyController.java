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
@RequestMapping("/usuarios")
public class UsuarioProxyController {

    @Autowired // Corregido: Inyección nativa directa de Spring sin depender de Lombok
    private RestTemplate restTemplate;

    @Value("${usuarios.service.url}")
    private String usuariosServiceUrl;

    @PostMapping("/sync")
    public ResponseEntity<String> sincronizar(@RequestHeader("Authorization") String authHeader, @RequestBody String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(usuariosServiceUrl + "/usuarios/sync", HttpMethod.POST, entity, String.class);
    }

    @GetMapping("/me")
    public ResponseEntity<String> miPerfil(@RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(usuariosServiceUrl + "/usuarios/me", HttpMethod.GET, entity, String.class);
    }
}
