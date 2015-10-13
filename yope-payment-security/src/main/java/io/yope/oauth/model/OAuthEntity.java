package io.yope.oauth.model;

import java.io.Serializable;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import lombok.Data;

@Data
public abstract class OAuthEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String tokenId;
	protected String authenticationId;
	protected String userName;
	protected String clientId;
	protected String refreshToken;
	protected byte[] oAuth2AccessToken;
	protected byte[] oauth2Request;
	protected byte[] authentication;

	protected Long lastLogin;

	public OAuthEntity() {}

	public OAuthEntity(final OAuth2AccessToken oAuth2AccessToken, final OAuth2Authentication authentication, final String authenticationId, final String jti, final String refreshJTI, final Long lastLogin) {
		this.tokenId = jti;
        this.oAuth2AccessToken = SerializationUtils.serialize(oAuth2AccessToken);
        this.authenticationId = authenticationId;
        this.userName = authentication.getName();
        this.oauth2Request = SerializationUtils.serialize(SerializationUtils.serialize(authentication.getOAuth2Request()));
        this.clientId = authentication.getOAuth2Request().getClientId();
        this.authentication = SerializationUtils.serialize(authentication);
        this.refreshToken = refreshJTI;
        this.lastLogin = lastLogin;
	}

	public byte[] getToken() {
		return oAuth2AccessToken;
	}

}
