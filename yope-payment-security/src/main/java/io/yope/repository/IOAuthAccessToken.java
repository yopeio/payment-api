package io.yope.repository;

import java.util.List;

import io.yope.oauth.model.OAuthAccessToken;
import io.yope.oauth.model.OAuthEntity;

public interface IOAuthAccessToken {
	
	OAuthEntity findByTokenId(String token);
	
	void deleteToken(String token);
	
	OAuthEntity findByRefreshToken(String refreshToken);

	List<OAuthEntity> findByAuthenticationId(String authenticationId);

    List<OAuthEntity> findByUserName(String userName);

    List<OAuthEntity> findByClientId(String clientId);
    
    List<OAuthEntity> findByClientIdAndUserName(String clientId, String userName);

	void delete(OAuthAccessToken storedToken);

	void saveOrUpdate(OAuthEntity storedOAuthEntity);
}
