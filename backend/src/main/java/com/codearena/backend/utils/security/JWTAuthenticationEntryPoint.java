package com.codearena.backend.utils.security;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

@Component
public class JWTAuthenticationEntryPoint  implements AuthenticationEntryPoint, Serializable {
    Logger logger = LoggerFactory.getLogger(JWTAuthenticationEntryPoint.class);
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        logger.debug("request path info, {}", request.getPathInfo());
//        PrintWriter writer = response.getWriter();
//        writer.println("Access Denied !!! " + authException.getMessage());
    }

}
