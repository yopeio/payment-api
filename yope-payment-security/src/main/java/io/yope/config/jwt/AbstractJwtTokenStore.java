package io.yope.config.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import io.yope.oauth.model.JWTCommon;
import io.yope.oauth.model.OAuthEntity;
import io.yope.oauth.model.OAuthRefreshToken;
import io.yope.repository.IOAuthAccessToken;
import io.yope.repository.IOAuthRefreshToken;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJwtTokenStore implements TokenStore {
	
	@Autowired protected IOAuthRefreshToken refreshTokenDao;
	
	protected final AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
	
	/*
	 * Force all extending sub classed of this one to inherit this functionality
	 */
	protected abstract IOAuthAccessToken getAccessDao();
	
	public abstract OAuth2Authentication readAuthentication(OAuth2AccessToken paramOAuth2AccessToken);
	
	public abstract void storeAccessToken(OAuth2AccessToken paramOAuth2AccessToken, OAuth2Authentication paramOAuth2Authentication);
	
	
	/**
	 * Refresh token's JTI is a valid UUID only when the token hasn't been refreshed.
	 * In any other case the JTI is the actual JWT signature of the old token.
	 * This method is traversing recursively through the JTI field until it finds the original UUID
	 * @param original the string representation for the JWT token.
	 * @return token's JTI value
	 */
	public static JWTCommon extractJtiFromRefreshToken(String original) {
		JWTCommon result = null;
		try {
			result = io.yope.utils.Serializer.createFromJson(io.yope.oauth.model.JWTCommon.class, JwtHelper.decode(original).getClaims());
			// this is not a valid UUID traverse
			while(result.getJti().length() > 36) {
				result = extractJtiFromRefreshToken(result.getJti());
			}
		} catch(Exception e) {
			result = new JWTCommon();
			result.setJti(original);
		}
		return result;
		
	}
	
	@Override
	public void storeRefreshToken(OAuth2RefreshToken paramOAuth2RefreshToken, OAuth2Authentication paramOAuth2Authentication) {
		JWTCommon common = extractJtiFromRefreshToken(paramOAuth2RefreshToken.getValue());
		refreshTokenDao.saveOrUpdate(new OAuthRefreshToken(paramOAuth2RefreshToken, paramOAuth2Authentication, common.getJti()));
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String paramString) {
		
		JWTCommon common = extractJtiFromRefreshToken(paramString);
		OAuthRefreshToken refreshEntity = refreshTokenDao.findByTokenId(common.getJti());
		if(refreshEntity == null)
			return null;
		return SerializationUtils.deserialize(refreshEntity.getOAuth2RefreshToken());
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken paramOAuth2RefreshToken) {
		JWTCommon common = extractJtiFromRefreshToken(paramOAuth2RefreshToken.getValue());
		OAuthRefreshToken storedObject = refreshTokenDao.findByTokenId(common.getJti());
		if(storedObject == null)
			return null;
		
		return SerializationUtils.deserialize(storedObject.getAuthentication());
	}
	
	protected OAuthEntity<?> readAuthenticationFromDB(String value) {
		JWTCommon common = extractJtiFromRefreshToken(value);
		
		OAuthEntity<?> storedObject = (OAuthEntity<?>) getAccessDao().findByTokenId(common.getJti());
		
		if(storedObject == null) {
			log.info("Stored OAuthEntity NOT FOUND" );
			return null;
		}
		
		return storedObject;
	}
	
	@Override
	public OAuth2Authentication readAuthentication(String paramString) {
		OAuthEntity<?> storedObject = readAuthenticationFromDB(paramString);
		
		return SerializationUtils.deserialize(storedObject.getAuthentication());
	}


	@Override
	public OAuth2AccessToken readAccessToken(final String paramString) {
		JWTCommon common = extractJtiFromRefreshToken(paramString);
		OAuthEntity<?> storedObject = (OAuthEntity<?>) getAccessDao().findByTokenId(common.getJti());
		if(storedObject == null)
			return null;
		Object authentication = SerializationUtils.deserialize(storedObject.getToken());
		
		return (OAuth2AccessToken) authentication;
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication paramOAuth2Authentication) {
		List<OAuthEntity<?>> storedObject = getAccessDao().findByAuthenticationId(authenticationKeyGenerator.extractKey(paramOAuth2Authentication));
		if(storedObject == null || storedObject.isEmpty()) {
			log.info("access Token is null ");
			return null;
		}
		
		final Object authentication = SerializationUtils.deserialize(storedObject.get(0).getToken());
		
		updateLastLogin(storedObject.get(0));

		return (OAuth2AccessToken) authentication;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientID, String userName) {
		List<OAuthEntity<?>> result = getAccessDao().findByClientIdAndUserName(clientID, userName);
		List<OAuth2AccessToken> oauthAccTokens = new ArrayList<>();
		for(OAuthEntity<?> token : result) {
			oauthAccTokens.add((OAuth2AccessToken) org.springframework.security.oauth2.common.util.SerializationUtils.deserialize(token.getoAuth2AccessToken()));
		}
		return oauthAccTokens;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String paramString) {
		List<OAuthEntity<?>> result = getAccessDao().findByClientId(paramString);
		List<OAuth2AccessToken> oauthAccTokens = new ArrayList<>();
		for(OAuthEntity<?> token : result) {
			oauthAccTokens.add((OAuth2AccessToken) SerializationUtils.deserialize(token.getoAuth2AccessToken()));
		}
		return oauthAccTokens;
	}
	
	public void updateLastLogin(OAuthEntity<?> storedOAuthEntity) {
		storedOAuthEntity.setLastLogin(System.currentTimeMillis());
		getAccessDao().saveOrUpdate(storedOAuthEntity);
	}
}
