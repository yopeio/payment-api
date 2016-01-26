package io.yope.payment.rest.resources;


import io.yope.domain.CurrentlyLoggedUser;
import io.yope.domain.YopeUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAuthority('ROLE_DOMAIN_USER')")
public class SampleController {

    @RequestMapping(value = "/stuff", method = RequestMethod.GET)
    public String getSomeStuff() {
        return "ok";
    }

    @RequestMapping(value = "/stuff", method = RequestMethod.POST)
    public void createStuff(@CurrentlyLoggedUser YopeUser domainUser) {
        return;
    }
}

