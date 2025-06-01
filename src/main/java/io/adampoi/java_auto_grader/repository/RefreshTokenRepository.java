package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.RefreshToken;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {


    Optional<RefreshToken> findByTokenAndExpiredAt(String token, Instant expiredAt);

    void deleteByToken(String token);


    boolean existsByUser(User user);

    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByTokenAndExpiredAtAfter(String token, Instant expiredAtAfter);

    Optional<RefreshToken> findByToken(String token);
}