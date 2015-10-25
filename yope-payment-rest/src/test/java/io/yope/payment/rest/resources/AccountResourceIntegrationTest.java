package io.yope.payment.rest.resources;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.containsString;

import org.junit.Test;
import org.springframework.util.Assert;

import com.jayway.restassured.RestAssured;

import io.yope.payment.rest.helpers.IntegrationTestHelper;

public class AccountResourceIntegrationTest {

	{
		RestAssured.baseURI = IntegrationTestHelper.LOCALHOST_GOLDRAKE;
	}

	@Test
	public void shouldGetVersionIntegrationTest() {
		get("/health").then().log().all()
			.assertThat()
			.statusCode(200);
			//.body(containsString("v01"));
	}

	@Test
	public void shouldOnlyCheckOAuthTest() {
		Assert.notNull(requestAccessToken());
	}

	static String requestAccessToken() {
		return IntegrationTestHelper.requestAccessToken("admin@yope.io", "Vit9uZ2S");
	}

}
