package io.yope.payment.mock.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.yope.payment.domain.Wallet;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Accessors(fluent=true) @JsonSerialize
public class WalletMock implements Wallet, Cloneable, Serializable {

    @JsonProperty private long id;
    @JsonProperty private String hash;
    @JsonProperty private BigDecimal balance;
    @JsonProperty private Status status;
    @JsonProperty private String name;
    @JsonProperty private String description;
    @JsonProperty private long creationDate;
    @JsonProperty private long modificationDate;
    @JsonProperty private Type type;
    @JsonProperty private byte[] content;
    @JsonProperty private String privateKey;

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
        return privateKey;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
