package io.yope.config.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.transaction.annotation.Transactional;

import io.yope.oauth.model.JWTCommon;
import io.yope.oauth.model.OAuthAccessToken;
import io.yope.oauth.model.OAuthRefreshToken;
import io.yope.repository.OAuthAccessTokenStore;
import io.yope.utils.BasicAuth;
import io.yope.utils.ThreadLocalUtils;

@Transactional(value="restTransactionManager", rollbackFor = Exception.class)
public class ServiceJwtTokenStore extends AbstractJwtTokenStore {
	
	private static final Logger log = LoggerFactory.getLogger(ServiceJwtTokenStore.class.getSimpleName());
	
	@Autowired private OAuthAccessTokenStore accessTokenDao;
	
	@Override
	protected OAuthAccessTokenStore getAccessDao() {
		return accessTokenDao;
	}
	
	@Override
	public OAuth2Authentication readAuthentication(final OAuth2AccessToken paramOAuth2AccessToken) {
		final OAuthAccessToken token = (OAuthAccessToken) readAuthenticationFromDB(paramOAuth2AccessToken.getValue());
		
		final OAuth2Authentication token2 = org.springframework.security.oauth2.common.util.SerializationUtils.deserialize(token.getAuthentication());
		log.info("Auth token {}", token2);
		
		// TODO XXX 
		User loggedUser = (User) token2.getPrincipal();
		User u = (User) token.getUser();
		log.info("------------------------------------");
		log.info("Logged User  {}", loggedUser);
		log.info("Registered User  {}", u);
		log.info("------------------------------------");
		//
		
		// does it have any sense?
		if(!u.getUsername().equals(loggedUser.getUsername())) {
			throw new InvalidTokenException("Invalid access token");
		}
			
		return token2;
	}
	
	@Override
	public void storeAccessToken(OAuth2AccessToken paramOAuth2AccessToken, OAuth2Authentication paramOAuth2Authentication) {
		JWTCommon accessJTI = io.yope.utils.Serializer.createFromJson(JWTCommon.class, JwtHelper.decode(paramOAuth2AccessToken.getValue()).getClaims());
		
		if (paramOAuth2AccessToken.getRefreshToken() != null) {
			JWTCommon refreshJTI = extractJtiFromRefreshToken(paramOAuth2AccessToken.getRefreshToken().getValue());
		}
		
		final String ip = BasicAuth.getClientIP(ThreadLocalUtils.currentRequest.get());
		final String remoteClientName = BasicAuth.getClientDevice(ThreadLocalUtils.currentRequest.get());
				
		OAuthAccessToken accessToken = null; // TODO refactor
		if(paramOAuth2AccessToken.getRefreshToken() != null) {
			JWTCommon refreshJTI = extractJtiFromRefreshToken(paramOAuth2AccessToken.getRefreshToken().getValue());
			accessToken = new OAuthAccessToken(paramOAuth2AccessToken, paramOAuth2Authentication, authenticationKeyGenerator.extractKey(paramOAuth2Authentication), accessJTI.getJti(), refreshJTI.getJti(), System.currentTimeMillis(), remoteClientName, ip);
		}
		else {
			accessToken = new OAuthAccessToken(paramOAuth2AccessToken, paramOAuth2Authentication, authenticationKeyGenerator.extractKey(paramOAuth2Authentication), accessJTI.getJti(), null, System.currentTimeMillis(), remoteClientName, ip);
		}
		log.info("Let´s save it... " + accessToken.getAuthenticationId());
	
		accessTokenDao.saveOrUpdate(accessToken);
	}


	@Override
	@Transactional(value="restTransactionManager", readOnly=false, rollbackFor = Exception.class)
	public void removeAccessToken(final OAuth2AccessToken paramOAuth2AccessToken) {
		JWTCommon common = extractJtiFromRefreshToken(paramOAuth2AccessToken.getValue());
		OAuthAccessToken storedObject = (OAuthAccessToken) getAccessDao().findByTokenId(common.getJti());
		if(storedObject != null) {
			getAccessDao().delete(storedObject);
		}
	}
	
	@Override
	@Transactional(value="restTransactionManager", readOnly=false, rollbackFor = Exception.class)
	public void removeRefreshToken(OAuth2RefreshToken paramOAuth2RefreshToken) {
		if(paramOAuth2RefreshToken == null) return;
		
		JWTCommon common = extractJtiFromRefreshToken(paramOAuth2RefreshToken.getValue());
		OAuthRefreshToken storedObject = refreshTokenDao.findByTokenId(common.getJti());
		if(storedObject != null) {
			refreshTokenDao.delete(storedObject);
		}
	
	}

	@Override
	@Transactional(value="restTransactionManager", readOnly=false, rollbackFor = Exception.class)
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken paramOAuth2RefreshToken) {
		if(paramOAuth2RefreshToken == null) return;
		
		JWTCommon common = extractJtiFromRefreshToken(paramOAuth2RefreshToken.getValue());
		OAuthAccessToken storedToken = (OAuthAccessToken) getAccessDao().findByRefreshToken(common.getJti());
		if(storedToken != null) {
			getAccessDao().delete(storedToken);
		}
	}
}



