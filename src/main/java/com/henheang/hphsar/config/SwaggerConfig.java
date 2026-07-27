package com.henheang.hphsar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration.
 * <p>
 * Adds a global "Bearer Token" security scheme so protected endpoints
 * can be tested directly from the Swagger UI without extra tools.
 * <p>
 * Access Swagger UI at: <a href="http://localhost:8080/swagger-ui/index.html">...</a>
 * <p>
 * How to test protected endpoints:
 *   1. Call POST /authorization/login to get a JWT token.
 *   2. Click the "Authorize" button (top right of Swagger UI).
 *   3. Enter:  Bearer <your-token>
 *   4. Click Authorize — all subsequent requests will include the header.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("H-char API")
                        .description("API for supplier-buyer wholesale operations")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}