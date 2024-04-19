package com.develop.thankyounext.infrastructure.config.security.jwt.driver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface JwtDriver {
    String createAccessToken(String email);

    String createRefreshToken();

    void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);

    Optional<String> extractRefreshToken(HttpServletRequest request);

    Optional<String> extractAccessToken(HttpServletRequest request);

    Optional<Long> extractId(String accessToken);

    void updateRefreshToken(Long id, String refreshToken);

    boolean isTokenValid(String token);

    Long getExpiration(String accessToken);
}
