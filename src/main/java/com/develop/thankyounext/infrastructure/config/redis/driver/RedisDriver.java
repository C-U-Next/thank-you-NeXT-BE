package com.develop.thankyounext.infrastructure.config.redis.driver;

import java.util.Optional;

public interface RedisDriver {
    void expireAccessToken(String accessToken, Long expiration);

    Optional<String> getLogoutStatus(String accessToken);

    void validBlackToken(String accessToken);
}
