package io.yope.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Wrapper to prevent No suitable constructor error:
 * com.fasterxml.jackson.databind.JsonMappingException: No suitable constructor found for type [simple type, class org.springframework.security.core.userdetails.User]: can not instantiate from JSON object (need to add/enable type information?)
 at [Source: io.netty.buffer.ByteBufInputStream@70f381a9; line: 1, column: 63]
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YopeUser {
    private Collection<String> roles;
    private String username;
    private String password;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
}
