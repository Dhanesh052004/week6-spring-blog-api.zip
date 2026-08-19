package com.blogapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * UI available at /swagger-ui.html, raw spec at /v3/api-docs.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI blogApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blog Management REST API")
                        .description("A production-quality RESTful API for managing blog posts, categories and comments.")
                        .version("1.0.0")
                        .contact(new Contact().name("Blog API Team").email("support@blogapi.example.com"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
    }
}
