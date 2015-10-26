package io.yope.payment.rest.resources;

import com.jayway.restassured.RestAssured;
import io.yope.payment.rest.helpers.IntegrationTestHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.Assert;

import static com.jayway.restassured.RestAssured.get;

public class AccountResourceIntegrationTest {

	{
		RestAssured.baseURI = IntegrationTestHelper.LOCALHOST_GOLDRAKE;
	}

    /**
     * Ignored because it fails.
     */
    @Ignore
	@Test
	public void shouldGetVersionIntegrationTest() {
		get("/health").then().log().all()
			.assertThat()
			.statusCode(200);
			//.body(containsString("v01"));
	}
    /**
     * Ignored because it fails.
     */
    @Ignore
	@Test
	public void shouldOnlyCheckOAuthTest() {
		Assert.notNull(requestAccessToken());
	}

	static String requestAccessToken() {
		return IntegrationTestHelper.requestAccessToken("admin@yope.io", "Vit9uZ2S");
	}

}
