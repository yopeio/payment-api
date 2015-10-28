package io.yope.payment.rest.resources;

import static com.jayway.restassured.RestAssured.given;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.Assert;

import com.jayway.restassured.RestAssured;

import io.yope.payment.domain.Account.Type;
import io.yope.payment.requests.RegistrationRequest;
import io.yope.payment.rest.helpers.IntegrationTestHelper;
import io.yope.utils.Serializer;

@lombok.extern.slf4j.Slf4j
public class AccountResourceIntegrationTest {

	{
		RestAssured.baseURI = IntegrationTestHelper.LOCALHOST_GOLDRAKE;
	}

	@Test
	public void shouldCreateAnAccountAngGetAnAccessTokenIntegrationTest() {
	    final RegistrationRequest registrationRequest = newRegistrationRequest();
	    
	    given()
            .contentType("application/json")
            .body(Serializer.json(registrationRequest))
            .post("/accounts").then()
            .log().all()
            .assertThat()
            .statusCode(201);
	    
	    log.debug("Registered: {}", registrationRequest);
	    
	    Assert.notNull(IntegrationTestHelper
	            .requestAccessToken(
	                    registrationRequest.getEmail(), 
	                    registrationRequest.getPassword()
	                    ));
	}

    private RegistrationRequest newRegistrationRequest() {
        return RegistrationRequest.builder()
                .email(RandomStringUtils.randomAlphabetic(8) + "@yope.io")
                .password(RandomStringUtils.randomAlphabetic(8))
                .firstName(RandomStringUtils.randomAlphabetic(8))
                .lastName(RandomStringUtils.randomAlphabetic(8))
                .name(RandomStringUtils.randomAlphabetic(8))
                .type(Type.SELLER)
                .build();
    }

    @Test @Ignore
	public void shouldOnlyCheckOAuthTest() {
	    System.err.println(requestAccessToken());
		Assert.notNull(requestAccessToken());
	}

	static String requestAccessToken() {
		return IntegrationTestHelper.requestAccessToken("admisssn@yope.io", "Vit9uZ2S");
	}

}
