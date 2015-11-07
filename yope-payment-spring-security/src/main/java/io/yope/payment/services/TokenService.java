package io.yope.payment.services;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final Cache restApiAuthTokenCache = CacheManager.getInstance().getCache("restApiAuthTokenCache");
    public static final int HALF_AN_HOUR_IN_MILLISECONDS = 30 * 60 * 1000;
    private static final int BEARER_LENGTH = "Bearer ".length();

    @Scheduled(fixedRate = HALF_AN_HOUR_IN_MILLISECONDS)
    public void evictExpiredTokens() {
        logger.info("Evicting expired tokens");
        restApiAuthTokenCache.evictExpiredElements();
    }

    public String generateNewToken() {
        return UUID.randomUUID().toString();
    }

    public void store(String token, Authentication authentication) {
        restApiAuthTokenCache.put(new Element(token, authentication));
    }

    public boolean contains(String token) {
        return restApiAuthTokenCache.get(extractToken(token)) != null;
    }

    public Authentication retrieve(String token) {
        return (Authentication) restApiAuthTokenCache.get(extractToken(token)).getObjectValue();
    }

    private String extractToken(String token) {
        return token.substring(BEARER_LENGTH);
    }
}
