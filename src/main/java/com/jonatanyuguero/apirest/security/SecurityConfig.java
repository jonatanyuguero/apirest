package com.jonatanyuguero.apirest.security;

import com.jonatanyuguero.apirest.error.CustomAccesDeniedHandler;
import com.jonatanyuguero.apirest.error.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAccesDeniedHandler customAccesDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                // Stateless: la API no usa sesión, en cada petición se valida el usuario
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(except -> {
                    except.authenticationEntryPoint(customAuthenticationEntryPoint);
                    except.accessDeniedHandler(customAccesDeniedHandler);
                })
                .authorizeHttpRequests(auth -> auth
                        // Documentación pública
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Consola H2 (sólo para desarrollo)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Registro público
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // Reglas de roles (las anotaciones @PreAuthorize de los controllers
                        // ya cubren la granularidad; aquí cerramos el resto del API).
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/categories/**").authenticated()
                        .requestMatchers("/tags/**").authenticated()
                        .requestMatchers("/tasks/**").authenticated()

                        .anyRequest().authenticated()
                );

        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(opts -> opts.disable()));

        return http.build();
    }
}
