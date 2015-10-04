package io.yope.oauth.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JWTCommon {
	
	private String jti;
	private Long exp;
	private String client_id;
	private List<UserRole> authorities;
	private String user_name;
}
