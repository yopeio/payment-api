package io.yope.oauth.model;

import java.io.Serializable;

import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OAuthRefreshToken implements Serializable{

	private static final long serialVersionUID = -917207147028399813L;
	
	private String tokenId;
    private byte[] oAuth2RefreshToken;
    private byte[] authentication;
	
	public OAuthRefreshToken() {
	}
    
    public OAuthRefreshToken(final OAuth2RefreshToken oAuth2RefreshToken, final OAuth2Authentication authentication, String jti) {
        this.oAuth2RefreshToken = SerializationUtils.serialize((Serializable) oAuth2RefreshToken);
        this.authentication = SerializationUtils.serialize((Serializable) authentication);
        this.tokenId = jti;
    }

	@Override
	public boolean equals(Object other) {
		return false;
	}
	@Override
	public int hashCode() {
		return 0;
	}
}
