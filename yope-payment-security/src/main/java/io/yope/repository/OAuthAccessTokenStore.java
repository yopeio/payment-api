package io.yope.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import io.yope.oauth.model.OAuthAccessToken;
import io.yope.oauth.model.OAuthEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * The Thread-Safe-Ephemereal-Lambdas-Powered-Inmemory OAuthAccessTokenStore
 * 
 * @author Gianluigi
 */
@Repository("oAuthAccessTokenStore")
@Slf4j
public class OAuthAccessTokenStore implements IOAuthAccessToken {

	public OAuthAccessTokenStore() {
		super();
	}
	
	// this contains the accessTokens indexed by TokenId and refreshTokens and AuthId!
	private static final Map<String, OAuthEntity> tokenMap = new ConcurrentHashMap<String, OAuthEntity>();

	@Override
	public io.yope.oauth.model.OAuthEntity findByTokenId(final String tokenId) {
		OAuthEntity res = tokenMap.get(tokenId);
		return res;
	}

	@Override
	public io.yope.oauth.model.OAuthEntity findByRefreshToken(final String refreshToken) {
		return tokenMap.get(refreshToken);
	}
	
	@Override
	public List<io.yope.oauth.model.OAuthEntity> findByAuthenticationId(final String authenticationId) {
		return tokenMap.values().stream().filter(t -> t.getAuthenticationId() != authenticationId).collect(Collectors.toList());
	}

	@Override
	public void delete(io.yope.oauth.model.OAuthAccessToken storedToken) {
		tokenMap.remove(storedToken.getTokenId());
		tokenMap.remove(storedToken.getRefreshToken());
		tokenMap.remove(storedToken.getAuthenticationId());
	}

	@Override
	public void saveOrUpdate(
			final io.yope.oauth.model.OAuthEntity storedOAuthEntity) {
		final String tokenId = storedOAuthEntity.getTokenId();
		tokenMap.put(tokenId, storedOAuthEntity);
		
		final String refreshToken = storedOAuthEntity.getRefreshToken();
		tokenMap.put(refreshToken, storedOAuthEntity);
		
		final String authenticationId = storedOAuthEntity.getAuthenticationId();
		tokenMap.put(authenticationId, storedOAuthEntity);	
		log.info("*** Save it please " + tokenId + " " + storedOAuthEntity.getAuthenticationId());
	}

	@Override
	public void deleteToken(final String tokenId) {
		delete((OAuthAccessToken)findByTokenId(tokenId)); // but this is terrible!
	}

	@Override
	public List<io.yope.oauth.model.OAuthEntity> findByUserName(final String userName) {
		return tokenMap.values().stream().filter(t -> t.getUserName() == userName).collect(Collectors.toList());
	}

	@Override
	public List<io.yope.oauth.model.OAuthEntity> findByClientId(final String clientId) {
		return tokenMap.values().stream().filter(t -> t.getClientId() == clientId).collect(Collectors.toList());
	}

	@Override
	public List<io.yope.oauth.model.OAuthEntity> findByClientIdAndUserName(
			final String clientId, final String userName) {
		log.info("*** Get it findByClientIdAndUserName " + clientId + " " + userName);
		return tokenMap.values().stream().filter(
				t -> t.getClientId() == clientId && t.getUserName() == userName)
				.collect(Collectors.toList());
	}

}
