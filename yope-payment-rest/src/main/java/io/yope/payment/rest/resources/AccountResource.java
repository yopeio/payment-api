package io.yope.payment.rest.resources;

import io.yope.payment.domain.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Account resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/transactions")
@Slf4j
public class AccountResource {
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody
    PaymentResponse<Account> createPlayer(
            final HttpSession session,
            final HttpServletRequest request,
            final HttpServletResponse response,
            @RequestBody(required=false) Account account) {
        return null;
    }

//    @RequestMapping(method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
//
//    @RequestMapping(value="/{playerId}", method = RequestMethod.GET, produces = "application/json")





}
