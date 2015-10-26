package io.yope.payment.configuration;

import org.redisson.Redisson;
import org.redisson.core.RMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import io.yope.config.jwt.ServiceJwtTokenStore;
import io.yope.oauth.model.OAuthEntity;
import io.yope.oauth.model.OAuthRefreshToken;
import io.yope.oauth.model.YopeUser;
import io.yope.payment.security.repositories.RedisUserRepository;
import io.yope.payment.security.repositories.UserRepository;
import io.yope.repository.IOAuthAccessToken;
import io.yope.repository.IOAuthRefreshToken;
import io.yope.repository.redis.OAuthAccessTokenRedisStore;
import io.yope.repository.redis.OAuthRefreshTokenRedisStore;

/**
 * Reference: http://projects.spring.io/spring-security-oauth/docs/oauth2.html
 *
 * @author Gianluigi
 */
@EnableResourceServer
@Import(SecurityConfig.class)
@Order(value = 2)
@Primary
public class OAuth2ServerConfig {

	public static final String API_USER_CLIENT = "my-trusted-wdpClient";
	private static final String API_RESOURCE = "oauth2-resource";

	@Configuration
	@EnableAuthorizationServer
	@Primary
	protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;

		private final static Integer accessTokenValiditySeconds = -1;
		private final static Integer refreshTokenValiditySeconds = -1;

		// OAuth2 security configurer
		@Override
		public void configure(final AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
			oauthServer.tokenKeyAccess("isAnonymous() || hasAuthority('ROLE_TRUSTED_CLIENT')")
					.checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')");
		}

		// OAuth2 endpoint configurer
		@Override
		public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints
				.authenticationManager(authenticationManager)
				.tokenStore(tokenStore(redisson()))
				.accessTokenConverter(accessTokenConverter());
		}

		@Bean
		public JwtAccessTokenConverter accessTokenConverter() {
			return new JwtAccessTokenConverter();
		}

		@Bean
		public TokenStore tokenStore(final Redisson redisson) {
			return new ServiceJwtTokenStore(oauthAccessTokenRedisStore(redisson), oauthRefreshTokenRedisStore(redisson), new DefaultAuthenticationKeyGenerator());
		}

		@Bean
		public Redisson redisson() {
			return Redisson.create();
		}

		@Bean
		public UserRepository redisUserRepository(final Redisson redisson) {
			final RMap<String, YopeUser> users = redisson.getMap("users");
			return new RedisUserRepository(users);
		}

		@Bean
		public IOAuthAccessToken oauthAccessTokenRedisStore(final Redisson redisson) {
			final RMap<String, OAuthEntity> tokens = redisson.getMap("accessTokens");
			return new OAuthAccessTokenRedisStore(tokens);
		}

		@Bean
		public IOAuthRefreshToken oauthRefreshTokenRedisStore(final Redisson redisson) {
			final RMap<String, OAuthRefreshToken> tokens = redisson.getMap("refreshTokens");
			return new OAuthRefreshTokenRedisStore(tokens);
		}

		@Override
		public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
			clients.inMemory().withClient(API_USER_CLIENT).resourceIds(API_RESOURCE)
					.authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
					.authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT").scopes("read", "write", "trust", "update")
					.accessTokenValiditySeconds(accessTokenValiditySeconds)
					.refreshTokenValiditySeconds(refreshTokenValiditySeconds).secret("secret");
		}
	}
}
