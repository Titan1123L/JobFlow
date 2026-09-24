package com.jobflow.api.service;

import com.jobflow.api.exception.InvalidResetTokenException;
import com.jobflow.common.email.EmailService;
import com.jobflow.common.entity.PasswordResetToken;
import com.jobflow.common.entity.User;
import com.jobflow.common.repository.PasswordResetTokenRepository;
import com.jobflow.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TOKEN_VALIDITY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public PasswordResetService(UserRepository userRepository,
                                 PasswordResetTokenRepository tokenRepository,
                                 EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    public void requestReset(String email) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        // Deliberately silent if the email doesn't exist - never reveal which emails have accounts
        if (maybeUser.isEmpty()) {
            return;
        }
        User user = maybeUser.get();

        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(sha256(rawToken));
        token.setExpiresAt(Instant.now().plus(TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES));
        tokenRepository.save(token);

        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
        String body = "Someone requested a password reset for your JobFlow account.\n\n"
                + "Click here to reset your password (link expires in 30 minutes):\n" + resetLink
                + "\n\nIf you didn't request this, you can safely ignore this email.";

        emailService.send(user.getEmail(), "Reset your JobFlow password", body);
    }

    public void confirmReset(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(InvalidResetTokenException::new);

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidResetTokenException();
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(InvalidResetTokenException::new);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e); // will never actually happen - SHA-256 is a JDK built-in
        }
    }
}
