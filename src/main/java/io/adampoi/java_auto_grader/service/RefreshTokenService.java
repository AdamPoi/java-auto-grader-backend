package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.RefreshToken;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.RefreshTokenRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public RefreshToken create(User user) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            refreshTokenRepository.delete(existingToken.get());
            refreshTokenRepository.flush();
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateRefreshToken(user))
                .expiredAt(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public String refresh(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndExpiredAtAfter(token, Instant.now())
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not valid or expired. Please login again."));

        return jwtService.generateToken(refreshToken.getUser());
    }


    public void delete(String refreshToken) {
        RefreshToken existedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not valid or expired. Please login again."));
        refreshTokenRepository.delete(existedRefreshToken);
    }

    public String generateRefreshToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRATION_MS)))
                .claim("userId", user.getId().toString())
                .claim("tokenType", "refresh")
                .signWith(key)
                .compact();
    }


}
