package com.project.auth_app_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiDocConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 1. Enforces security requirements across your endpoint collection paths
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // 2. Defines the UI security interface mechanics (JWT Bearer Strategy)
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide your active JWT Access Token to clear permission boundaries for secured endpoints.")))
                // 3. Application Portfolio and Identity Info Block
                .info(new Info()
                        .title("Application Core Ecosystem API Reference Hub")
                        .version("1.0.0")
                        .description("Enterprise-ready backend server platform handling user onboarding, identity claims execution, and automated stateless security tokens.")
                        .contact(new Contact()
                                .name("Core Platform Engineering Team")
                                .email("dev-support@project.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}