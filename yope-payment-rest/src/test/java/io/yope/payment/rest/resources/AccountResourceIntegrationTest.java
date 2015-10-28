package io.yope.payment.rest.resources;

import org.junit.Test;
import org.springframework.util.Assert;

import com.jayway.restassured.RestAssured;

import io.yope.payment.rest.helpers.IntegrationTestHelper;

@lombok.extern.slf4j.Slf4j
public class AccountResourceIntegrationTest {

	{
		RestAssured.baseURI = IntegrationTestHelper.LOCALHOST_GOLDRAKE;
	}

	@Test
	public void shouldCreateAnAccountAngGetAnAccessTokenIntegrationTest() {
	    log.info("POST /account test");
	    Assert.notNull(IntegrationTestHelper.requestAccountAndAccessToken());
	}

}
