package com.teamcollab.backend.security;

import com.teamcollab.backend.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Without this, a missing/invalid JWT gets rejected by Spring Security's filter chain
 * BEFORE the request ever reaches a controller - so GlobalExceptionHandler never sees it,
 * and the client gets an empty response body instead of our usual JSON error shape.
 * This plugs that gap for the "no valid credentials at all" (401) case.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), "Unauthorized", "Authentication is required");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
