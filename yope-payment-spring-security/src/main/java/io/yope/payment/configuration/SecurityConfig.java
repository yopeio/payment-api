package io.yope.payment.configuration;

import io.yope.payment.db.services.UserSecurityService;
import io.yope.payment.domain.YopeUser;
import io.yope.payment.filters.AuthenticationFilter;
import io.yope.payment.filters.ManagementEndpointAuthenticationFilter;
import io.yope.payment.repository.RedisUserRepository;
import io.yope.payment.repository.UserRepository;
import io.yope.payment.services.*;
import org.redisson.Redisson;
import org.redisson.core.RMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;


@Configuration
@EnableWebMvcSecurity
@EnableScheduling
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${backend.admin.role}")
    private String backendAdminRole;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
//                csrf().disable().
//                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
//                and().
//                authorizeRequests().
//                antMatchers(actuatorEndpoints()).hasRole(backendAdminRole).
//                anyRequest().authenticated().
//                and().
//                anonymous().disable().
//                exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint());

//        http.
                csrf().disable().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().
                authorizeRequests().
                and().requestMatchers()
                // SECURE IT
                .antMatchers("/authenticate/**", "/accounts/**", "/wallets/**","/transactions/**",
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
                .antMatchers(HttpMethod.GET, "/secured/route/oauth").fullyAuthenticated()
                .antMatchers(actuatorEndpoints()).hasRole(backendAdminRole).
//                anyRequest().authenticated().
                and().
                anonymous().disable().
                exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint());

        http.addFilterBefore(new AuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class).
                addFilterBefore(new ManagementEndpointAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class);
    }

    private String[] actuatorEndpoints() {
        return new String[]{ApiController.AUTOCONFIG_ENDPOINT, ApiController.BEANS_ENDPOINT, ApiController.CONFIGPROPS_ENDPOINT,
                ApiController.ENV_ENDPOINT, ApiController.MAPPINGS_ENDPOINT,
                ApiController.METRICS_ENDPOINT, ApiController.SHUTDOWN_ENDPOINT};
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(domainUsernamePasswordAuthenticationProvider()).
                authenticationProvider(backendAdminUsernamePasswordAuthenticationProvider()).
                authenticationProvider(tokenAuthenticationProvider());
    }

    @Bean
    public TokenService tokenService() {
        return new TokenService();
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
    public ExternalServiceAuthenticator userServiceAuthenticator() {
        return new UserServiceAuthenticator();
    }

    @Bean
    public AuthenticationProvider domainUsernamePasswordAuthenticationProvider() {
        return new DomainUsernamePasswordAuthenticationProvider(tokenService(), userServiceAuthenticator());
    }

    @Bean
    public AuthenticationProvider backendAdminUsernamePasswordAuthenticationProvider() {
        return new BackendAdminUsernamePasswordAuthenticationProvider();
    }

    @Bean
    public AuthenticationProvider tokenAuthenticationProvider() {
        return new TokenAuthenticationProvider(tokenService());
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}