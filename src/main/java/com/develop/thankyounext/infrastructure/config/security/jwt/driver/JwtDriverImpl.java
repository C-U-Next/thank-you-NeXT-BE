package com.develop.thankyounext.infrastructure.config.security.jwt.driver;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.develop.thankyounext.domain.repository.member.MemberRepository;
import com.develop.thankyounext.infrastructure.config.redis.driver.RedisDriver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Getter
@Slf4j
public class JwtDriverImpl implements JwtDriver {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String ID_CLAIM = "email";
    private static final String BEARER_CLAIM = "bearer ";

    private final MemberRepository memberRepository;
    private final RedisDriver redisDriver;

    @Override
    public String createAccessToken(String email) {
        Date now = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
                .withClaim(ID_CLAIM, email)
                .sign(Algorithm.HMAC512(secretKey));
    }

    @Override
    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    @Override
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
    }

    @Override
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER_CLAIM))
                .map(refreshToken -> refreshToken.replace(BEARER_CLAIM, ""));
    }

    @Override
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER_CLAIM))
                .map(refreshToken -> refreshToken.replace(BEARER_CLAIM, ""));
    }

    @Override
    public Optional<Long> extractId(String accessToken) {
        Optional<String> isLogout = redisDriver.getLogoutStatus(accessToken);
        if (isLogout.isEmpty()) {
            try {
                return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                        .build()
                        .verify(accessToken)
                        .getClaim(ID_CLAIM)
                        .asLong()
                );
            } catch (Exception e) {
                log.error("액세스 토큰이 유효하지 않습니다.");
            }
        }

        return Optional.empty();
    }

    @Override
    public void updateRefreshToken(Long id, String refreshToken) {
        memberRepository.findById(id)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> {
                            throw new RuntimeException("일치하는 회원이 없습니다.");
                        }
                );
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long getExpiration(String accessToken) {
        Date expiration = JWT.require(Algorithm.HMAC512(secretKey)).build().verify(accessToken)
                .getExpiresAt();
        return expiration.getTime() - new Date().getTime();
    }

    private void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    private void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }
}
