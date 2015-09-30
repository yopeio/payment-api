package io.yope.payment.mock.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.yope.payment.domain.Wallet;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(fluent=true) @JsonSerialize
public class WalletMock implements Wallet {

    private long id;
    private String hash;
    private BigDecimal balance;
    private Status status;
    private String name;
    private String description;
    private long creationDate;
    private long modificationDate;
    private Type type;
    private byte[] content;
    private String publicKey;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Long getCreationDate() {
        return creationDate;
    }

    @Override
    public Long getModificationDate() {
        return modificationDate;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public String getPrivateKey() {
        return publicKey;
    }
}
