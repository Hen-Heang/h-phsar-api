package com.henheang.hphsar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henheang.hphsar.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JwtAuthenticationEntryPoint — Handles 401 Unauthorized Errors
 * <p>
 * This is triggered by Spring Security when a request reaches a protected
 * endpoint but has NO valid authentication (no token, expired token, etc.).
 * <p>
 * Without this, Spring would return an HTML error page.
 * With this, we return a clean JSON response like:
 * {
 *   "status": 401,
 *   "message": "Unauthorized access",
 *   "error": "Full authentication is required..."
 * }
 * <p>
 * Flow:
 *   Request (no/invalid token)
 *     → JwtRequestFilter (fails to authenticate)
 *     → Spring Security blocks request
 *     → JwtAuthenticationEntryPoint.commence() is called
 *     → Returns 401 JSON response to client
 * <p>
 * Registered in: SecurityConfig → .authenticationEntryPoint(jwtAuthenticationEntryPoint)
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Set HTTP status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Tell the client the response body is JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Build a structured error response
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized access",
                authException.getMessage() != null
                        ? authException.getMessage()
                        : "Full authentication is required. Please provide a valid Bearer token."
        );

        // Write the error object as JSON directly to the HTTP response
        new ObjectMapper().writeValue(response.getOutputStream(), apiErrorResponse);
    }
}
