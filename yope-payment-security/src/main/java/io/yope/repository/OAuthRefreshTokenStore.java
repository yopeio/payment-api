package io.yope.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import io.yope.oauth.model.OAuthRefreshToken;

@Repository("oAuthRefreshTokenStore")
public class OAuthRefreshTokenStore implements IOAuthRefreshToken {

	private static Map<String, OAuthRefreshToken> tokenMap = new ConcurrentHashMap<String, OAuthRefreshToken>();
	
    public OAuthRefreshTokenStore() {
        super();
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