package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.RefreshToken;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.RefreshTokenRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class RefreshTokenService {

    final
    RefreshTokenRepository refreshTokenRepository;
    final
    UserRepository userRepository;
    private final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds
    private final JwtService jwtService;
    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;

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
                .expiredAt(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public String refresh(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndExpiredAtAfter(token, Instant.now())
                .orElseThrow(() -> new AccessDeniedException("Refresh token not valid or expired. Please login again."));

        return jwtService.generateToken(refreshToken.getUser());
    }


    public void delete(String refreshToken) {
        RefreshToken existedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AccessDeniedException("Refresh token not valid or expired. Please login again."));
        if (existedRefreshToken != null) {
            refreshTokenRepository.delete(existedRefreshToken);
        }
    }

    public String generateRefreshToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .claim("userId", user.getId().toString())
                .claim("tokenType", "refresh")
                .signWith(key)
                .compact();
    }


}