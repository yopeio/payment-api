package io.yope.payment.configuration;

import org.redisson.Redisson;
import org.redisson.core.RMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.client.RestTemplate;

import io.yope.auth.AccessDeniedExceptionHandler;
import io.yope.auth.UnauthorizedEntryPoint;
import io.yope.oauth.model.OAuthEntity;
import io.yope.oauth.model.OAuthRefreshToken;
import io.yope.oauth.model.YopeUser;
import io.yope.payment.security.repositories.RedisUserRepository;
import io.yope.payment.security.repositories.UserRepository;
import io.yope.repository.IOAuthAccessToken;
import io.yope.repository.IOAuthRefreshToken;
import io.yope.repository.OAuthAccessTokenStore;
import io.yope.repository.OAuthRefreshTokenStore;
import io.yope.repository.redis.OAuthAccessTokenRedisStore;
import io.yope.repository.redis.OAuthRefreshTokenRedisStore;
import io.yope.repository.user.UserService;
import io.yope.repository.user.UserServiceNoSqlImpl;

@EnableWebSecurity
@Primary
@EnableAsync
@EnableAutoConfiguration
@EnableResourceServer
@ComponentScan({
    "io.yope.payment.security.services",
    "io.yope.payment.security.repositories",
    })
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired private AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired private AccessDeniedHandler accessDeniedHandler;
    @Autowired private PasswordEncoder passEncoder;

    @Autowired
    private UserService userService;

    @Override
    /** This is the configuration for the OAuth module */
    public void configure(final WebSecurity web) throws Exception {
        web.debug(false).ignoring()
                .antMatchers(HttpMethod.POST, "/accounts/**")
                .antMatchers(HttpMethod.POST, "/mailchimp/**")
                .antMatchers(HttpMethod.GET, "/mailchimp/**")
                .antMatchers("/webjars/**", "/images/**", "/qr/**", "/registration/**",
                        "/oauth/uncache_approvals", "/oauth/cache_approvals")
                .and().ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() { // no enconding for the time
                                               // being
        return new PasswordEncoder() {

            @Override
            public boolean matches(final CharSequence rawPassword,
                    final String encodedPassword) {
                return true;
            }

            @Override
            public String encode(final CharSequence rawPassword) {
                return rawPassword.toString();
            }
        };
    }

    @Bean
    public UserService userService() {
        return new UserServiceNoSqlImpl();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passEncoder);
    }

    @Override
    /** This is the configuration for the security module itself */
    protected void configure(final HttpSecurity http) throws Exception {

        ContentNegotiationStrategy contentNegotiationStrategy = http
                .getSharedObject(ContentNegotiationStrategy.class);
        if (contentNegotiationStrategy == null) {
            contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
        }
        final MediaTypeRequestMatcher preferredMatcher = new MediaTypeRequestMatcher(
                contentNegotiationStrategy,
                MediaType.APPLICATION_FORM_URLENCODED,
                MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA);

        http.anonymous().disable().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                // handle access denied in general (for example comming from
                // @PreAuthorization
                .authenticationEntryPoint(authenticationEntryPoint)
                // handle authentication exceptions for unauthorized calls.
                .defaultAuthenticationEntryPointFor(authenticationEntryPoint,
                        preferredMatcher)
                .and().requestMatchers()
                // SECURE IT
                .antMatchers("/accounts/**", "/wallets/**",
                        "/postident/**", "/idnow/**", "/webid/**", "/me/**",
                        "/api/version", "/secured/route/oauth")
                .and().headers().contentTypeOptions().cacheControl()
                .frameOptions().httpStrictTransportSecurity().xssProtection()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/accounts/**").fullyAuthenticated()
                .antMatchers(HttpMethod.PUT, "/accounts/**").fullyAuthenticated()
                .antMatchers(HttpMethod.DELETE, "/accounts/**").fullyAuthenticated()
                .antMatchers("/wallets/**").fullyAuthenticated()
                .antMatchers("/transactions/**").fullyAuthenticated()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/users/shadow").permitAll()
                .antMatchers(HttpMethod.GET, "/secured/route/oauth").fullyAuthenticated();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedExceptionHandler();
    }

    @Bean
    AuthenticationEntryPoint entryPointBean() {
        return new UnauthorizedEntryPoint();
    }

    @Bean
    IOAuthAccessToken ioAuthAccessToken() {
        return new OAuthAccessTokenStore();
    }

    @Bean
    IOAuthRefreshToken iOAuthRefreshToken() {
        return new OAuthRefreshTokenStore();
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
    
    
}
