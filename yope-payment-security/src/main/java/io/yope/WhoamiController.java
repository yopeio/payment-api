package io.yope;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
public class WhoamiController {
	
    private static final Logger log = LoggerFactory.getLogger(WhoamiController.class.getSimpleName());

    private static final String COM_NUMBER26_MICROSERVICE_WHOAMI = "com.number26.router";

    @RequestMapping("/whoami") @ResponseBody
    public String home() {
        log.info("Called whoami on {}", new Date());
        return COM_NUMBER26_MICROSERVICE_WHOAMI;
    }
}
