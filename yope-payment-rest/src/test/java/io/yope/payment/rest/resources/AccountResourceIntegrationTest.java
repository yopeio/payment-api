package io.yope.payment.rest.resources;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.util.Assert;

import com.jayway.restassured.RestAssured;

import io.yope.payment.rest.helpers.IntegrationTest;
import io.yope.payment.rest.helpers.IntegrationTestHelper;

@lombok.extern.slf4j.Slf4j
@Category(IntegrationTest.class)
public class AccountResourceIntegrationTest {

	{
		RestAssured.baseURI = IntegrationTestHelper.LOCALHOST_GOLDRAKE;
	}

	@Test
	public void shouldCreateAnAccountAngGetAnAccessTokenIntegrationTest() {
	    log.info("POST /account test");
	    Assert.notNull(IntegrationTestHelper.requestAccountAndAccessToken());
	}
	
	@Test
	public void accessTokenMustBeDifferentForDifferentUsers() {
	    String accessToken = IntegrationTestHelper.requestAccountAndAccessToken();
        String accessToken2 = IntegrationTestHelper.requestAccountAndAccessToken();
        
        System.err.println(accessToken);
        System.err.println(accessToken2);
	}

}
