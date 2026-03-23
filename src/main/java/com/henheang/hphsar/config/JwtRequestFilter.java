package com.henheang.hphsar.config;

import com.henheang.hphsar.service.implement.JwtUserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * <p>
 * Intercepts every incoming HTTP request (once per request) to:
 * 1. Extract the JWT token from the "Authorization: Bearer <token>" header
 * 2. Validate the token and extract the user's email
 * 3. Load the user from the database
 * 4. Set authentication in Spring Security context if the token is valid
 * <p>
 * This allows protected endpoints to know who the current user is.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUserDetailsServiceImpl jwtUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(JwtUserDetailsServiceImpl jwtUserDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String email = null;
        String jwtToken = null;

        // Step 1: Extract token from "Bearer <token>" header
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7); // remove "Bearer " prefix
            try {
                email = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                // Token is invalid or expired — skip authentication and continue the chain.
                // Spring Security will enforce access rules: public endpoints will be allowed,
                // protected endpoints will return 401 via JwtAuthenticationEntryPoint.
                logger.warn("JWT Token is invalid or expired: " + e.getMessage());
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        // Step 2: Validate token and set authentication in Security context
        // Only proceed if email was extracted and user is not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from the database by email
            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(email);

            // Step 3: If token is valid, authenticate the user in Spring Security
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Mark user as authenticated for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue the filter chain to the next filter or controller
        chain.doFilter(request, response);
    }
}
