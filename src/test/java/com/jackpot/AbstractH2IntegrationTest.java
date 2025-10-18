package com.jackpot;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Abstract base class for integration tests using H2 in-memory database.
 * Use this for faster tests that don't require PostgreSQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractH2IntegrationTest {
    // H2 configuration comes from application-test.yml
}
