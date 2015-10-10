package io.yope.oauth.model;

import java.io.Serializable;
import java.util.Arrays;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public abstract class OAuthEntity<T>  implements Serializable {

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(authentication);
		result = prime * result + (authenticationId == null ? 0 : authenticationId.hashCode());
		result = prime * result + (clientId == null ? 0 : clientId.hashCode());
		result = prime * result + Arrays.hashCode(oAuth2AccessToken);
		result = prime * result + Arrays.hashCode(oauth2Request);
		result = prime * result + (refreshToken == null ? 0 : refreshToken.hashCode());
		result = prime * result + (tokenId == null ? 0 : tokenId.hashCode());
		result = prime * result + (userName == null ? 0 : userName.hashCode());
		result = prime * result + (lastLogin == null ? 0 : lastLogin.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (!(obj instanceof OAuthEntity)) {
            return false;
        }
		final OAuthEntity<?> other = (OAuthEntity<?>) obj;
		if (!Arrays.equals(authentication, other.authentication)) {
            return false;
        }
		if (authenticationId == null) {
			if (other.authenticationId != null) {
                return false;
            }
		} else if (!authenticationId.equals(other.authenticationId)) {
            return false;
        }
		if (clientId == null) {
			if (other.clientId != null) {
                return false;
            }
		} else if (!clientId.equals(other.clientId)) {
            return false;
        }
		if (!Arrays.equals(oAuth2AccessToken, other.oAuth2AccessToken)) {
            return false;
        }
		if (!Arrays.equals(oauth2Request, other.oauth2Request)) {
            return false;
        }
		if (refreshToken == null) {
			if (other.refreshToken != null) {
                return false;
            }
		} else if (!refreshToken.equals(other.refreshToken)) {
            return false;
        }
		if (tokenId == null) {
			if (other.tokenId != null) {
                return false;
            }
		} else if (!tokenId.equals(other.tokenId)) {
            return false;
        }
		if (userName == null) {
			if (other.userName != null) {
                return false;
            }
		} else if (!userName.equals(other.userName)) {
            return false;
        }
		if (lastLogin == null) {
			if (other.lastLogin != null) {
                return false;
            }
		} else if (!lastLogin.equals(other.lastLogin)) {
            return false;
        }
		return true;
	}

	public void setToken(final byte[] token) {
		this.oAuth2AccessToken = token;
	}

	public String getAuthenticationId() {
		return authenticationId;
	}

	public void setAuthenticationId(final String authentication_id) {
		this.authenticationId = authentication_id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String user_name) {
		this.userName = user_name;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(final String client_id) {
		this.clientId = client_id;
	}

	public byte[] getAuthentication() {
		return authentication;
	}

	public void setAuthentication(final byte[] authentication) {
		this.authentication = authentication;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(final String refresh_token) {
		this.refreshToken = refresh_token;
	}


	public byte[] getOauth2Request() {
		return oauth2Request;
	}

	public void setOauth2Request(final byte[] oauth2Request) {
		this.oauth2Request = oauth2Request;
	}
	public String getTokenStr() {
		return tokenId;
	}

	public void setTokenStr(final String tokenStr) {
		this.tokenId = tokenStr;
	}

	public byte[] getToken() {
		return oAuth2AccessToken;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(final String tokenId) {
		this.tokenId = tokenId;
	}

	public byte[] getoAuth2AccessToken() {
		return oAuth2AccessToken;
	}

	public void setoAuth2AccessToken(final byte[] oAuth2AccessToken) {
		this.oAuth2AccessToken = oAuth2AccessToken;
	}

	public Long getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(final Long lastLogin) {
		this.lastLogin = lastLogin;
	}

}
