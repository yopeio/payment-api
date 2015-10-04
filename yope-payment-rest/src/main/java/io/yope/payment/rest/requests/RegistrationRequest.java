/**
 *
 */
package io.yope.payment.rest.requests;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Getter;

/**
 * @author mgerardi
 *
 */
@Getter
public class RegistrationRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

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
