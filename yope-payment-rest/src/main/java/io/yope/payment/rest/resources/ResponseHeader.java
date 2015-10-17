package io.yope.payment.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Data
@Accessors(fluent=true)
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResponseHeader {
    @JsonProperty private boolean success;
    @JsonProperty private Integer status;
}
