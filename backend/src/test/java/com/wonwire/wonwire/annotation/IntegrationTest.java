package com.wonwire.wonwire.annotation;

import com.wonwire.wonwire.config.EmbeddedRedisConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation for controller integration tests.
 *
 * Bundles the standard test setup shared by all controller test classes:
 *   - @SpringBootTest: starts the full application context
 *   - @AutoConfigureMockMvc: configures MockMvc for HTTP-level testing
 *   - @ActiveProfiles("test"): loads application-test.yaml (H2 + embedded Redis)
 *   - @DirtiesContext(AFTER_EACH_TEST_METHOD): recreates the Spring context after
 *       each test, ensuring a clean H2 database and fresh embedded Redis instance.
 *       Required because rate-limit counters are keyed by IP (always 127.0.0.1 in
 *       MockMvc) and would bleed across tests without a full reset.
 *   - @Import(EmbeddedRedisConfig): starts the embedded Redis server on port 6379.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(EmbeddedRedisConfig.class)
public @interface IntegrationTest {
}