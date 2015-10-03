package io.yope.oauth.model;

import java.util.List;

public class JWTCommon {
	
	private String jti;
	private Long exp;
	private String client_id;
	private List<UserRole> authorities;
	private String user_name;
	
	public Long getExp() {
		return exp;
	}
	public void setExp(Long exp) {
		this.exp = exp;
	}
	public String getJti() {
		return jti;
	}
	public void setJti(String jti) {
		this.jti = jti;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public List<UserRole> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(List<UserRole> authorities) {
		this.authorities = authorities;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
}
