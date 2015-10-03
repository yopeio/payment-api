package io.yope.oauth.model;

import java.io.Serializable;

import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

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
    
	/**
	 * @return the tokenId
	 */
	public String getTokenId() {
		return tokenId;
	}



	/**
	 * @param tokenId the tokenId to set
	 */
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}



	/**
	 * @return the oAuth2RefreshToken
	 */
	public byte[] getoAuth2RefreshToken() {
		return oAuth2RefreshToken;
	}



	/**
	 * @param oAuth2RefreshToken the oAuth2RefreshToken to set
	 */
	public void setoAuth2RefreshToken(byte[] oAuth2RefreshToken) {
		this.oAuth2RefreshToken = oAuth2RefreshToken;
	}



	/**
	 * @return the authentication
	 */
	public byte[] getAuthentication() {
		return authentication;
	}



	/**
	 * @param authentication the authentication to set
	 */
	public void setAuthentication(byte[] authentication) {
		this.authentication = authentication;
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
