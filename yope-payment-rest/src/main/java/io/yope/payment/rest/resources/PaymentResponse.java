package io.yope.payment.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@AllArgsConstructor
@Data
@Accessors(fluent=true)
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PaymentResponse<T> {
    @JsonProperty private ResponseHeader header;
    @JsonProperty private T body;
}
