package org.apache.camel.example.springboot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { Application.class })
@ActiveProfiles("test-first")
class FirstRecipientApplicationTest {

    @Test
    void testApplication() {
        Assertions.assertTrue(true);
    }
}
