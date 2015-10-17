package io.yope.payment.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent=true)
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class PaymentResponse<T> {

    @JsonProperty private ResponseHeader header;
    @JsonProperty private final T body;
    @JsonProperty private Error error;

    public PaymentResponse(final ResponseHeader header, final T body) {
        this(header, body, null);
    }

    public PaymentResponse(final ResponseHeader header, final Error error) {
        this(header, null, error);
    }

}
