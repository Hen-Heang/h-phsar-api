package com.henheang.hphsar.config;

import com.henheang.hphsar.service.implement.JwtUserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 * <p>
 * Defines the entire security setup for this application:
 * - Role-based access control per endpoint
 * - Stateless JWT authentication (no server-side sessions)
 * - Custom 401 handler for unauthenticated requests
 * - JWT filter runs before Spring's default auth filter
 */
@Configuration
public class SecurityConfig {

    private final JwtUserDetailsServiceImpl jwtUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(
            JwtUserDetailsServiceImpl jwtUserDetailsService,
            PasswordEncoder passwordEncoder,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtRequestFilter jwtRequestFilter) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Configures how Spring loads and verifies user credentials.
     * Uses the database (via jwtUserDetailsService) + Bcrypt password encoder.
     */
    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(jwtUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Main security filter chain — defines access rules for all endpoints.
     * <p>
     * Public endpoints (no token needed):
     *   - /authorization/**       → login, register, OTP, etc.
     *   - /api/v1/files/**        → file uploads/downloads
     *   - /v3/api-docs/**, /swagger-ui/** → API documentation
     * <p>
     * Protected endpoints (token required):
     *   - /api/v1/retailer/**     → RETAILER role only
     *   - /api/v1/distributor/**  → DISTRIBUTOR role only
     *   - anything else           → any authenticated user
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable) // disable CSRF since we use stateless JWT, not cookies
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/retailer/**").hasAuthority("RETAILER")
                .requestMatchers("/api/v1/distributor/**").hasAuthority("DISTRIBUTOR")
                .requestMatchers(
                        "/authorization/**",
                        "/api/v1/files/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/ws/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            // Return 401 JSON response when user is not authenticated
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            // Stateless: no HTTP session is created or used — every request must carry a JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(daoAuthenticationProvider());

        // Run JwtRequestFilter before Spring's default username/password filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    /**
     * Exposes the AuthenticationManager bean so it can be injected into
     * the login controller to manually authenticate users.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
