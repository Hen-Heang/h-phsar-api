package com.henheang.hphsar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 * <p>
 * CORS is a browser security rule that blocks frontend apps from calling an API
 * on a different domain unless the API explicitly allows it.
 * <p>
 * Example without this config:
 *   Frontend on <a href="http://localhost:3000">...</a> calls API on <a href="http://localhost:8080">...</a>
 *   → Browser blocks the request with "CORS error"
 * <p>
 * Example with this config:
 *   → Request is allowed because the API says "all origins are permitted"
 * <p>
 * This filter runs BEFORE Spring Security so that preflight OPTIONS requests
 * (sent by the browser before the real request) are not blocked by auth checks.
 * <p>
 * Related: SecurityConfig disables its own CORS handling (.cors().disable())
 *          because this CorsFilter already handles it at the servlet level.
 */
@Configuration
public class CorsFilterConfiguration {

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, Authorization headers) to be sent cross-origin
        config.setAllowCredentials(true);

        // Allow requests from any domain (frontend URL)
        // Use setAllowedOrigins("https://yourdomain.com") in production for security
        config.setAllowedOriginPatterns(Collections.singletonList("*"));

        // Allow these headers to be included in cross-origin requests
        config.setAllowedHeaders(Arrays.asList(
                "X-Requested-With", "Origin", "Content-Type", "Accept",
                "Authorization",                          // JWT token header
                "Access-Control-Allow-Credentials",
                "Access-Control-Allow-Headers",
                "Access-Control-Allow-Methods",
                "Access-Control-Allow-Origin",
                "Access-Control-Expose-Headers",
                "Access-Control-Max-Age",
                "Access-Control-Request-Headers",
                "Access-Control-Request-Method",
                "Age", "Allow", "Alternates",
                "Content-Range", "Content-Disposition", "Content-Description"
        ));

        // Allow these HTTP methods from cross-origin requests
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));

        // Apply this CORS policy to every endpoint in the application
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}