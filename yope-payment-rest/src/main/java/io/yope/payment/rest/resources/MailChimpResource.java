package io.yope.payment.rest.resources;

import groovy.util.logging.Slf4j;
import io.yope.payment.exceptions.DuplicateEmailException;
import io.yope.payment.rest.helpers.AccountHelper;
import io.yope.payment.rest.requests.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Mailchimp resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/mailchimp")
@Slf4j
public class MailChimpResource extends BaseResource {

    @Autowired
    AccountHelper accountHelper;

    @RequestMapping(method=RequestMethod.GET)
    @ResponseBody
    String notified() {
        return "Ok";
    }

    @RequestMapping(method=RequestMethod.POST)
    @ResponseBody
    String confirmNotified(@RequestParam(value="data[merges][EMAIL]") String email,
                           @RequestParam(value="data[merges][FNAME]") String firstName,
                           @RequestParam(value="data[merges][LNAME]") String lastName,
                           @RequestParam(value="data[merges][PWD]") String password,
                           @RequestParam(value="data[merges][WALLET_NM]") String wName,
                           @RequestParam(value="data[merges][WALLET_DSC]") String wDesc,
                           @RequestParam(value="data[merges][WALLET_HSH]") String wHash

    ) {
        RegistrationRequest registration =
                RegistrationRequest.builder()
                        .description(wDesc)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .hash(wHash)
                        .password(password)
                        .name(wName)
                        .build();
        try {
            accountHelper.registerAccount(registration);
        } catch (DuplicateEmailException e) {
            // voluntarily empty.
            return "Ko";
        }
        return "Ok";
    }

}
