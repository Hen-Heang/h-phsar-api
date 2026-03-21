package com.henheang.stock_flow_commerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.UnauthorizedException;
import com.henheang.stock_flow_commerce.model.ApiErrorResponse;
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
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
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
