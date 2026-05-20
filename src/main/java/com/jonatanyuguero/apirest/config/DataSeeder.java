package com.jonatanyuguero.apirest.config;

import com.jonatanyuguero.apirest.users.User;
import com.jonatanyuguero.apirest.users.UserRepository;
import com.jonatanyuguero.apirest.users.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inserta un usuario ADMIN por defecto al arrancar la aplicación,
 * para poder probar los endpoints protegidos por rol.
 *  username: admin
 *  password: admin
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findFirstByUsername("admin").isEmpty()) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@apirest.local")
                    .fullname("Administrador")
                    .password(passwordEncoder.encode("admin"))
                    .role(UserRole.ADMIN)
                    .build());
        }
    }
}
