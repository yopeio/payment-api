/**
 *
 */
package io.yope.payment.rest;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author massi
 *
 */
@Configuration
@ConditionalOnBean(type = "ServerConfiguration")
public class StaticResourceConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private ServerConfiguration serverConfiguration;

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        String path = new File(serverConfiguration.getImageFolder()).getAbsolutePath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        registry.addResourceHandler(serverConfiguration.getImagePath()+"/**").addResourceLocations("file:"+path);
        super.addResourceHandlers(registry);
    }
}