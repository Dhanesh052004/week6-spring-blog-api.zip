package com.blogapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the Blog Management REST API application.
 */
@SpringBootApplication
public class BlogApiApplication {

    private static final Logger log = LoggerFactory.getLogger(BlogApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BlogApiApplication.class, args);
        log.info("=================================================");
        log.info(" Blog Management API started successfully");
        log.info(" Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info(" H2 Console: http://localhost:8080/h2-console");
        log.info("=================================================");
    }
}
