/**
 *
 */
package io.yope.payment.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author mgerardi
 *
 */
@Builder(builderClassName="Builder", toBuilder=true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString(of= {"type", "email"}, includeFieldNames = false)
public class Account {

    public enum Status {
        ACTIVE,
        DEACTIVATED,
        PENDING,
        SUSPENDED;
    }

    public enum Type {
        SELLER, BUYER, ADMIN;
    }

    private Type type;

    private Long id;

    private String email;

    @JsonIgnore
    private List<Wallet> wallets;

    private String firstName;

    private String lastName;

    private Status status;

    private Long registrationDate;

    private Long modificationDate;


}
