package com.alexbezsh.ecommerce.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver exceptionResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(config -> config.configure(http))
            .authorizeHttpRequests(config ->
                config.requestMatchers(GET, "/api/v1/products", "/swagger/**",
                        "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers(OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated())
            .oauth2ResourceServer(config ->
                config.jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(authEntryPoint()))
            .sessionManagement(config -> config.sessionCreationPolicy(STATELESS))
            .build();
    }

    @Bean
    public AuthenticationEntryPoint authEntryPoint() {
        return (req, res, e) -> exceptionResolver.resolveException(req, res, null, e);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<String> roles = jwt.getClaim("groups");
            return Optional.ofNullable(roles)
                .orElse(Collections.emptyList())
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        return authConverter;
    }

}
