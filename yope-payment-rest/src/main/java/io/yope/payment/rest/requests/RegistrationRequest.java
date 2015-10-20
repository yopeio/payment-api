/**
 *
 */
package io.yope.payment.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import io.yope.payment.domain.Account.Type;
import lombok.Getter;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String password;

    @NotBlank
    private Type type = Type.SELLER;

    /*
     * Wallet details
     */

    /**
     * the name of the first internal wallet.
     */
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
