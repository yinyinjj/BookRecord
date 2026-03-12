package com.bookrecord.service;

import com.bookrecord.dto.JwtAuthenticationResponse;
import com.bookrecord.dto.LoginRequest;
import com.bookrecord.dto.RegisterRequest;
import com.bookrecord.dto.UserInfo;
import com.bookrecord.entity.User;
import com.bookrecord.exception.BadRequestException;
import com.bookrecord.repository.UserRepository;
import com.bookrecord.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public UserInfo register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        return UserInfo.fromEntity(user);
    }

    @Transactional
    public JwtAuthenticationResponse login(LoginRequest request) {
        log.info("User logging in: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication.getName());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        log.info("User logged in successfully: {}", user.getUsername());

        return JwtAuthenticationResponse.of(jwt, jwtExpiration / 1000, UserInfo.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return UserInfo.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}