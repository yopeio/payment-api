package io.yope.payment.rest.helpers;

import com.jayway.restassured.response.Header;
import io.yope.payment.domain.Account.Type;
import io.yope.payment.requests.RegistrationRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import static com.jayway.restassured.RestAssured.given;

/**
 * Integration test helper
 * 
 * @author Gianluigi
 *
 */
@Slf4j
public abstract class IntegrationTestHelper {
	
	public static final String LIVE_GOLDRAKE = "https://somewhereovertheraimbow.io";
	public static final String LOCALHOST_GOLDRAKE = "http://127.0.0.1:8080/";
	
	public static final int NOT_AUTHORIZED = 401;
	public static final int OK = 200;
	
	public static String requestAccountAndAccessToken() {
	    final RegistrationRequest registrationRequest = newRegistrationRequest();
        
//        given()
//            .contentType("application/json")
//            .body(Serializer.json(registrationRequest))
//            .post("/accounts").then()
//            .log().all()
//            .assertThat()
//            .statusCode(201);
        
        log.debug("Registered: {}", registrationRequest);
        return IntegrationTestHelper
                .requestAccessToken(
                        registrationRequest.getEmail(), 
                        registrationRequest.getPassword()
                        );
	}

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
	
	 private static RegistrationRequest newRegistrationRequest() {
	        return RegistrationRequest.builder()
	                .email(RandomStringUtils.randomAlphabetic(8) + "@yope.io")
	                .password(RandomStringUtils.randomAlphabetic(8))
	                .firstName(RandomStringUtils.randomAlphabetic(8))
	                .lastName(RandomStringUtils.randomAlphabetic(8))
	                .name(RandomStringUtils.randomAlphabetic(8))
	                .type(Type.SELLER)
	                .build();
	    }
}

