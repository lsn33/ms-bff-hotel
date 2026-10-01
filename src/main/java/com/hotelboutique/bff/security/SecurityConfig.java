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

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences:ecc9edec-2abe-4cec-9a71-f60b2826c339}")
    private String expectedAudience;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight del navegador: no lleva token ni ejecuta lógica de negocio
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Catálogo público o de consulta
                        .requestMatchers(HttpMethod.GET, "/habitaciones/disponibles").permitAll()
                        .requestMatchers(HttpMethod.POST, "/habitaciones").hasRole("ADMIN")
                        
                        // Gestión de reservas segmentada por rol (Rúbrica Caso 5)
                        .requestMatchers(HttpMethod.POST, "/reservas").hasAnyRole("ADMIN", "CLIENTE") // Permitir crear a ambos
                        .requestMatchers(HttpMethod.GET, "/reservas").hasAnyRole("ADMIN", "CLIENTE")  // Filtro interno o global de visualización
                        
                        // Acciones puramente administrativas (Check-In / Check-Out de Recepción)
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/checkin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/checkout").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/cancelar").hasAnyRole("ADMIN", "CLIENTE") // Ambos actores pueden cancelar reservas
                        
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(entraAuthConverter())));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Habilitamos localhost para desarrollo local y patrones dinámicos si se despliega en CloudFront/S3
        config.setAllowedOriginPatterns(List.of("http://localhost:4200", "https://*.aws.com", "http://*.aws.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("authorization", "content-type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

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
            // Validar audience de forma estricta
            validateAudience(jwt);

            // Extrae roles de Entra ID
            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null || roles.isEmpty()) {
                return List.of();
            }

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Concatena el prefijo esperado por .hasRole()
                    .collect(Collectors.toList());
        });

        return converter;
    }

    private void validateAudience(Jwt jwt) {
        List<String> tokenAud = jwt.getAudience();

        // Validación adaptada para arreglar casos donde Microsoft envía múltiples audiences en formato de lista
        if (tokenAud == null || tokenAud.stream().noneMatch(aud -> aud.equals(expectedAudience))) {
            throw new IllegalArgumentException(
                    String.format("Invalid audience. Expected: %s, got: %s", expectedAudience, tokenAud)
            );
        }
    }
}
