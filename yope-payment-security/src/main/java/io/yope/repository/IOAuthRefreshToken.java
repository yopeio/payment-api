package io.yope.repository;

import io.yope.oauth.model.OAuthRefreshToken;

public interface IOAuthRefreshToken {

	OAuthRefreshToken findByTokenId(String tokenId);
	
	void deleteByToken(String tokenID);

	void saveOrUpdate(OAuthRefreshToken oAuthRefreshToken);

	void delete(OAuthRefreshToken storedObject);
}
