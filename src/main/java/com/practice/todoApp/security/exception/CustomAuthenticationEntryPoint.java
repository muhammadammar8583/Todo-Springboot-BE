package com.practice.todoApp.security.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        
        String requestURI = request.getRequestURI();
        
        // Skip custom handling for auth endpoints - let AuthController handle errors
        if (requestURI.startsWith("/api/auth/")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
            return;
        }
        
        // For other endpoints, return custom JSON response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = "{\"success\":false,\"message\":\"Unauthorized\",\"data\":null}";
        response.getWriter().write(jsonResponse);
    }
}
