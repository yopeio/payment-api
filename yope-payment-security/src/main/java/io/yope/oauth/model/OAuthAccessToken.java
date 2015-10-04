package io.yope.oauth.model;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OAuthAccessToken extends OAuthEntity<OAuthAccessToken>{

	private static final long serialVersionUID = -7945135597484875770L;

	private User user;
	private String userAgent;
	private String remoteIP;
	
	public OAuthAccessToken() {}

	public OAuthAccessToken(final OAuth2AccessToken oAuth2AccessToken, final OAuth2Authentication authentication, final String authenticationId, String jti, String refreshJTI, Long lastLogin, String userAgent, String remoteIP) {
       super(oAuth2AccessToken, authentication, authenticationId, jti, refreshJTI, lastLogin);
       this.user = (User) authentication.getUserAuthentication().getPrincipal();
       this.userAgent = userAgent;
       this.remoteIP = remoteIP;
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((remoteIP == null) ? 0 : remoteIP.hashCode());
		result = prime * result + ((userAgent == null) ? 0 : userAgent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof OAuthAccessToken))
			return false;
		OAuthAccessToken other = (OAuthAccessToken) obj;
		if (remoteIP == null) {
			if (other.remoteIP != null)
				return false;
		} else if (!remoteIP.equals(other.remoteIP))
			return false;
		if (userAgent == null) {
			if (other.userAgent != null)
				return false;
		} else if (!userAgent.equals(other.userAgent))
			return false;
		return true;
	}

}
