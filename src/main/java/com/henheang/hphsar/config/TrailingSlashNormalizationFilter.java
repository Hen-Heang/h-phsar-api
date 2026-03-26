package com.henheang.hphsar.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Strips trailing slashes from incoming request URIs before they reach Spring Security
 * or any controller. This prevents 401/404 mismatches caused by the frontend sending
 * URLs like /api/v1/distributor/profiles/ when the mapped endpoint is /api/v1/distributor/profiles.
 * <p>
 * Runs first in the filter chain (HIGHEST_PRECEDENCE) so the normalized URI is seen
 * by every downstream filter including Spring Security and JwtRequestFilter.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrailingSlashNormalizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Strip trailing slash only when the URI has more than just "/"
        if (uri.length() > 1 && uri.endsWith("/")) {
            String strippedUri = uri.substring(0, uri.length() - 1);

            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getRequestURI() {
                    return strippedUri;
                }

                @Override
                public StringBuffer getRequestURL() {
                    StringBuffer original = request.getRequestURL();
                    return new StringBuffer(original.substring(0, original.length() - 1));
                }
            };

            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}