package com.jonatanyuguero.apirest.users;

import com.jonatanyuguero.apirest.error.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(NewUserCommand cmd) {
        User user = User.builder()
                .username(cmd.username())
                .email(cmd.email())
                .fullname(cmd.fullname())
                .password(passwordEncoder.encode(cmd.password()))
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User findByUsername(String username) {
        return userRepository.findFirstByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public User editProfile(User current, EditProfileCommand cmd) {
        if (cmd.email() != null && !cmd.email().isBlank()) current.setEmail(cmd.email());
        if (cmd.fullname() != null && !cmd.fullname().isBlank()) current.setFullname(cmd.fullname());
        if (cmd.password() != null && !cmd.password().isBlank())
            current.setPassword(passwordEncoder.encode(cmd.password()));
        return userRepository.save(current);
    }

    public User promoteToGestor(Long id) {
        User u = findById(id);
        u.setRole(UserRole.GESTOR);
        return userRepository.save(u);
    }

    public User demoteToUser(Long id) {
        User u = findById(id);
        u.setRole(UserRole.USER);
        return userRepository.save(u);
    }

    public User adminUpdateUser(Long id, NewUserCommand cmd) {
        User u = findById(id);
        if (cmd.username() != null && !cmd.username().isBlank()) u.setUsername(cmd.username());
        if (cmd.email() != null && !cmd.email().isBlank()) u.setEmail(cmd.email());
        if (cmd.fullname() != null && !cmd.fullname().isBlank()) u.setFullname(cmd.fullname());
        if (cmd.password() != null && !cmd.password().isBlank())
            u.setPassword(passwordEncoder.encode(cmd.password()));
        return userRepository.save(u);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id))
            throw new UserNotFoundException(id);
        userRepository.deleteById(id);
    }
}
