package io.yope.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class AccessDeniedExceptionHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException arg2) throws IOException, ServletException {
    	if(!response.isCommitted())
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, arg2.getMessage());
    }


}
