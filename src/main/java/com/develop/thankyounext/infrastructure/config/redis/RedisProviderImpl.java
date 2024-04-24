package com.develop.thankyounext.infrastructure.config.redis;

import com.develop.thankyounext.global.exception.handler.TokenHandler;
import com.develop.thankyounext.global.payload.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class RedisProviderImpl implements RedisProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Value
    private static final String LOGOUT_VALUE = "logout";

    @Override
    public void expireAccessToken(String accessToken, Long expiration) {
        redisTemplate.opsForValue().set(accessToken, LOGOUT_VALUE, Duration.ofSeconds(expiration));
    }

    @Override
    public Optional<String> getLogoutStatus(String accessToken) {
        return Optional.ofNullable((String) redisTemplate.opsForValue().get(accessToken));
    }

    @Override
    public void validBlackToken(String accessToken) {
        String blackToken = (String) redisTemplate.opsForValue().get(accessToken);
        if (StringUtils.hasText(blackToken)) {
            throw new TokenHandler(ErrorStatus.TOKEN_ALREADY_EXISTED);
        }
    }
}
