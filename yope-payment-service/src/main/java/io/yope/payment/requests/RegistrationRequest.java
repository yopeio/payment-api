/**
 *
 */
package io.yope.payment.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import io.yope.payment.domain.Account.Type;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    private static final String ALPHANUMERIC_ERROR_MSG = "Must contain alphanumeric characters";
    private static final String ALPHANUMERIC_PATTERN = "^[A-Za-z0-9]+$";
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 30;

    @Email
    private String email;

    @NotBlank
    @Pattern(message=ALPHANUMERIC_ERROR_MSG, regexp = ALPHANUMERIC_PATTERN)
    @Size(min = MIN_SIZE, max = MAX_SIZE)
    private String firstName;

    @NotBlank
    @Pattern(message=ALPHANUMERIC_ERROR_MSG, regexp = ALPHANUMERIC_PATTERN)
    @Size(min = MIN_SIZE, max = MAX_SIZE)
    private String lastName;

    @NotBlank
    @Pattern(message=ALPHANUMERIC_ERROR_MSG, regexp = ALPHANUMERIC_PATTERN)
    @Size(min = MIN_SIZE, max = MAX_SIZE)
    private String password;

    private Type type = Type.SELLER;

    /*
     * Wallet details
     */

    /**
     * the name of the first internal wallet.
     */
    @NotBlank
    @Pattern(message=ALPHANUMERIC_ERROR_MSG, regexp = ALPHANUMERIC_PATTERN)
    @Size(min = MIN_SIZE, max = MAX_SIZE)
    private String name;

    /**
     * the description of the first internal wallet.
     */
    private String description;

    /**
     * the hash of the external wallet.
     */
    private String hash;
}
