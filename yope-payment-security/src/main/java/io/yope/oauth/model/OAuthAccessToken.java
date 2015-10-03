package io.yope.oauth.model;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public class OAuthAccessToken extends OAuthEntity<OAuthAccessToken>{

	private static final long serialVersionUID = -7945135597484875770L;

	private User user;
	private String userAgent;
	private String remoteIP;
	
	public OAuthAccessToken() {
    }

	public OAuthAccessToken(final OAuth2AccessToken oAuth2AccessToken, final OAuth2Authentication authentication, final String authenticationId, String jti, String refreshJTI, Long lastLogin, String userAgent, String remoteIP) {
       super(oAuth2AccessToken, authentication, authenticationId, jti, refreshJTI, lastLogin);
       this.user = (User) authentication.getUserAuthentication().getPrincipal();
       this.userAgent = userAgent;
       this.remoteIP = remoteIP;
    }
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((remoteIP == null) ? 0 : remoteIP.hashCode());
		result = prime * result + ((userAgent == null) ? 0 : userAgent.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
