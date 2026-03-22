package com.henheang.hphsar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.UnauthorizedException;
import com.henheang.hphsar.model.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized access",
                authException.getMessage() != null ? authException.getMessage() : "Full authentication is required to access this resource. Please provide a valid Bearer token."
        );

        new ObjectMapper().writeValue(response.getOutputStream(), apiErrorResponse);
    }
//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//        // Create the response body
//        ObjectMapper objectMapper = new ObjectMapper();
//        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
//                HttpStatus.UNAUTHORIZED,
//                "Unauthorized access",
//                authException.getMessage()
//        );
//        String responseBody = objectMapper.writeValueAsString(apiErrorResponse);
//
//        // Write the response body to the output stream
//        PrintWriter writer = response.getWriter();
//        writer.write(responseBody);
//        writer.flush();
//    }
}
