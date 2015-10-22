package io.yope.payment.rest.helpers;

import static com.jayway.restassured.RestAssured.given;

import com.jayway.restassured.response.Header;

/**
 * Integration test helper
 * 
 * @author Gianluigi
 *
 */
public abstract class IntegrationTestHelper {
	
	public static final String LIVE_GOLDRAKE = "https://somewhereovertheraimbow.io";
	public static final String LOCALHOST_GOLDRAKE = "http://127.0.0.1:8080/";
	
	public static final int NOT_AUTHORIZED = 401;
	public static final int OK = 200;

	public static String requestAccessToken(final String username, final String password) {
		return given()
			.header(new Header("Authorization", "Basic bXktdHJ1c3RlZC13ZHBDbGllbnQ6c2VjcmV0"))
			.and()
			.param("grant_type", "password")
			.param("username", username)
			.param("password", password)
			
			.then()
			.post("/oauth/token")
			.jsonPath().getString("access_token");
	}
}