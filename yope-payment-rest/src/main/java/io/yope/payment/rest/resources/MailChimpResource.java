package io.yope.payment.rest.resources;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import groovy.util.logging.Slf4j;
import io.yope.payment.exceptions.DuplicateEmailException;
import io.yope.payment.requests.RegistrationRequest;

/**
 * Mailchimp resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/mailchimp")
@Slf4j
public class MailChimpResource extends BaseResource {

    @RequestMapping(method=RequestMethod.GET)
    @ResponseBody
    String notified() {
        return "Ok";
    }

    @RequestMapping(method=RequestMethod.POST)
    @ResponseBody
    String confirmNotified(@RequestParam(value="data[merges][EMAIL]") final String email,
                           @RequestParam(value="data[merges][FNAME]") final String firstName,
                           @RequestParam(value="data[merges][LNAME]") final String lastName,
                           @RequestParam(value="data[merges][PWD]") final String password,
                           @RequestParam(value="data[merges][WALLET_NM]") final String wName,
                           @RequestParam(value="data[merges][WALLET_DSC]") final String wDesc,
                           @RequestParam(value="data[merges][WALLET_HSH]") final String wHash

    ) {
        final RegistrationRequest registration =
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
            accountService.registerAccount(registration);
        } catch (final DuplicateEmailException e) {
            // voluntarily empty.
            return "Ko";
        }
        return "Ok";
    }

}
