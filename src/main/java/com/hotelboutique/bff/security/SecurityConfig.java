package com.hotelboutique.bff.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences:}")
    private String audiences;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight del navegador: no lleva token ni ejecuta logica de negocio
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/habitaciones/disponibles").permitAll()
                        .requestMatchers(HttpMethod.POST, "/habitaciones").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reservas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/checkin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/checkout").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/cancelar").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(entraAuthConverter())));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("authorization", "content-type"));
        config.setMaxAge(600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private Converter<Jwt, AbstractAuthenticationToken> entraAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Principal claim: use preferred_username (UPN) de Entra ID
        converter.setPrincipalClaimName("preferred_username");

        // Converter para roles (claims)
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Validar audience
            validateAudience(jwt);

            // Extrae roles de Entra ID
            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null || roles.isEmpty()) {
                return List.of();
            }

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });

        return converter;
    }

    private void validateAudience(Jwt jwt) {
        String tokenAud = jwt.getClaimAsString(JwtClaimNames.AUD);

        // La app del API que configura este BFF es el audience esperado
        String expectedAud = "ecc9edec-2abe-4cec-9a71-f60b2826c339";

        if (tokenAud == null || !tokenAud.equals(expectedAud)) {
            throw new IllegalArgumentException(
                    String.format("Invalid audience. Expected: %s, got: %s", expectedAud, tokenAud)
            );
        }
    }
}