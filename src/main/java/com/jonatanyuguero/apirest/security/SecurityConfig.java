package com.jonatanyuguero.apirest.security;

import com.jonatanyuguero.apirest.error.CustomAccesDeniedHandler;
import com.jonatanyuguero.apirest.error.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAccesDeniedHandler customAccesDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception{

        http
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(except ->{
                    except.authenticationEntryPoint(customAuthenticationEntryPoint);
                    except.accessDeniedHandler(customAccesDeniedHandler);
                })
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                        .requestMatchers("/auth/register").permitAll()
                        .anyRequest().authenticated()


                );
        http.csrf(csfr -> csfr.disable());
        http.headers(headers -> headers.frameOptions(opts -> opts.disable()));



        return http.build();
    }
}
