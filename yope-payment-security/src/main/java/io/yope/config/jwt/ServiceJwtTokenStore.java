package io.yope.config.jwt;

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
import io.yope.repository.IOAuthAccessToken;
import io.yope.utils.BasicAuth;
import io.yope.utils.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;

@Transactional(value="neo4jTransactionManager", rollbackFor = Exception.class) @Slf4j
public class ServiceJwtTokenStore extends AbstractJwtTokenStore {

	@Autowired private IOAuthAccessToken accessTokenRepository;

	@Override
	protected IOAuthAccessToken getAccessDao() {
		return accessTokenRepository;
	}

	@Override
	public OAuth2Authentication readAuthentication(final OAuth2AccessToken paramOAuth2AccessToken) {
		final OAuthAccessToken token = (OAuthAccessToken) readAuthenticationFromDB(paramOAuth2AccessToken.getValue());

		final OAuth2Authentication token2 = org.springframework.security.oauth2.common.util.SerializationUtils.deserialize(token.getAuthentication());
		log.info("Auth token {}", token2);

		// TODO XXX
		final User loggedUser = (User) token2.getPrincipal();
		final User u = token.getUser();
		log.info("------------------------------------");
		log.info("Logged User  {}", loggedUser);
		log.info("Registered User  {}", u);
		log.info("------------------------------------");
		
		if(!u.getUsername().equals(loggedUser.getUsername())) {
			throw new InvalidTokenException("Invalid access token");
		}

		return token2;
	}

	@Override
	public void storeAccessToken(final OAuth2AccessToken paramOAuth2AccessToken, final OAuth2Authentication paramOAuth2Authentication) {
		final JWTCommon accessJTI = io.yope.utils.Serializer.createFromJson(JWTCommon.class, JwtHelper.decode(paramOAuth2AccessToken.getValue()).getClaims());

		if (paramOAuth2AccessToken.getRefreshToken() != null) {
			extractJtiFromRefreshToken(paramOAuth2AccessToken.getRefreshToken().getValue());
		}

		final String ip = BasicAuth.getClientIP(ThreadLocalUtils.currentRequest.get());
		final String remoteClientName = BasicAuth.getClientDevice(ThreadLocalUtils.currentRequest.get());

		OAuthAccessToken accessToken = null; // TODO refactor
		if(paramOAuth2AccessToken.getRefreshToken() != null) {
			final JWTCommon refreshJTI = extractJtiFromRefreshToken(paramOAuth2AccessToken.getRefreshToken().getValue());
			accessToken = new OAuthAccessToken(paramOAuth2AccessToken, paramOAuth2Authentication, authenticationKeyGenerator.extractKey(paramOAuth2Authentication), accessJTI.getJti(), refreshJTI.getJti(), System.currentTimeMillis(), remoteClientName, ip);
		}
		else {
			accessToken = new OAuthAccessToken(paramOAuth2AccessToken, paramOAuth2Authentication, authenticationKeyGenerator.extractKey(paramOAuth2Authentication), accessJTI.getJti(), null, System.currentTimeMillis(), remoteClientName, ip);
		}
		log.info("Let´s save it... " + accessToken.getAuthenticationId());

		accessTokenRepository.saveOrUpdate(accessToken);
	}


	@Override
	@Transactional(value="neo4jTransactionManager", readOnly=false, rollbackFor = Exception.class)
	public void removeAccessToken(final OAuth2AccessToken paramOAuth2AccessToken) {
		final JWTCommon common = extractJtiFromRefreshToken(paramOAuth2AccessToken.getValue());
		final OAuthAccessToken storedObject = (OAuthAccessToken) getAccessDao().findByTokenId(common.getJti());
		if(storedObject != null) {
			getAccessDao().delete(storedObject);
		}
	}

	@Override
	@Transactional(value="neo4jTransactionManager", readOnly=false, rollbackFor = Exception.class)
	public void removeRefreshToken(final OAuth2RefreshToken paramOAuth2RefreshToken) {
		if(paramOAuth2RefreshToken == null) {
            return;
        }

		final JWTCommon common = extractJtiFromRefreshToken(paramOAuth2RefreshToken.getValue());
		final OAuthRefreshToken storedObject = refreshTokenDao.findByTokenId(common.getJti());
		if(storedObject != null) {
			refreshTokenDao.delete(storedObject);
		}

	}

	@Override
	@Transactional(value="neo4jTransactionManager", readOnly=false, rollbackFor = Exception.class)
	public void removeAccessTokenUsingRefreshToken(final OAuth2RefreshToken paramOAuth2RefreshToken) {
		if(paramOAuth2RefreshToken == null) {
            return;
        }

		final JWTCommon common = extractJtiFromRefreshToken(paramOAuth2RefreshToken.getValue());
		final OAuthAccessToken storedToken = (OAuthAccessToken) getAccessDao().findByRefreshToken(common.getJti());
		if(storedToken != null) {
			getAccessDao().delete(storedToken);
		}
	}
}



