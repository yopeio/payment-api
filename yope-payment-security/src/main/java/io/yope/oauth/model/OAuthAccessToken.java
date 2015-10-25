package io.yope.oauth.model;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAccessToken extends OAuthEntity {

	private static final long serialVersionUID = -7945135597484875770L;

	private User user;
	private String userAgent;
	private String remoteIP;

	public OAuthAccessToken(final OAuth2AccessToken oAuth2AccessToken, final OAuth2Authentication authentication, final String authenticationId, String jti, String refreshJTI, Long lastLogin, String userAgent, String remoteIP) {
       super(oAuth2AccessToken, authentication, authenticationId, jti, refreshJTI, lastLogin);
       this.user = (User) authentication.getUserAuthentication().getPrincipal();
       this.userAgent = userAgent;
       this.remoteIP = remoteIP;
    }
}
