package com.myprojects.urlshortener.service;

import com.myprojects.urlshortener.entity.Role;
import com.myprojects.urlshortener.entity.User;
import com.myprojects.urlshortener.repository.UserRepository;
import com.myprojects.urlshortener.web.form.RegisterUserForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(RegisterUserForm userForm) {
        if (userRepository.existsByEmail(userForm.email())) {
            throw new RuntimeException("Email already exists");
        }
        var user = new User();
        user.setEmail(userForm.email());
        user.setPassword(passwordEncoder.encode(userForm.password()));
        user.setName(userForm.name());
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}