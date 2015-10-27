package io.yope.oauth.model;

import com.google.common.collect.Lists;
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

	private YopeUser user;
	private String userAgent;
	private String remoteIP;

	public OAuthAccessToken(final OAuth2AccessToken oAuth2AccessToken, final OAuth2Authentication authentication, final String authenticationId, String jti, String refreshJTI, Long lastLogin, String userAgent, String remoteIP) {
       super(oAuth2AccessToken, authentication, authenticationId, jti, refreshJTI, lastLogin);
       User user = ((User) authentication.getUserAuthentication().getPrincipal());
       YopeUser yopeUser = YopeUser.builder().
                user(user.getUsername()).
                pwd(user.getPassword()).
                authorities(Lists.newArrayList(user.getAuthorities().iterator().next().getAuthority())).
                build();
       this.user = yopeUser;
       this.userAgent = userAgent;
       this.remoteIP = remoteIP;
    }
}
