package com.blogapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class BlogApiApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts successfully
        // with all beans wired correctly.
    }
}
