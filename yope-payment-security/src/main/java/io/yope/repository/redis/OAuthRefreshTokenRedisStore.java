package io.yope.repository.redis;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import io.yope.oauth.model.OAuthRefreshToken;
import io.yope.repository.IOAuthRefreshToken;

@Repository @Qualifier(OAuthRefreshTokenRedisStore.QUALIFIER)
public class OAuthRefreshTokenRedisStore implements IOAuthRefreshToken {
	
	public static final String QUALIFIER = "oAuthRefreshTokenRedisStore";

	private final Map<String, OAuthRefreshToken> tokenMap;
	
    public OAuthRefreshTokenRedisStore(final Map<String, OAuthRefreshToken> tokenMap) {
        this.tokenMap = tokenMap;
    }
    
	@Override
	public OAuthRefreshToken findByTokenId(final String tokenId) {
		return tokenMap.get(tokenId);
	}

	@Override
	public void deleteByToken(String tokenID) {
		final OAuthRefreshToken entity = findByTokenId(tokenID);
        delete(entity);
	}

	@Override
	public void saveOrUpdate(final OAuthRefreshToken oAuthRefreshToken) {
		tokenMap.put(oAuthRefreshToken.getTokenId(), oAuthRefreshToken);
	}

	@Override
	public void delete(final OAuthRefreshToken storedObject) {
		tokenMap.remove(storedObject.getTokenId());
	}
}