package com.develop.thankyounext.infrastructure.config.redis;

import java.util.Optional;

public interface RedisProvider {
    void expireAccessToken(String accessToken, Long expiration);

    Optional<String> getLogoutStatus(String accessToken);

    void validBlackToken(String accessToken);
}
