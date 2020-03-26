package dev.ebullient.it.micrometer.prometheus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * tests that application.properties is read from src/test/resources
 *
 * This does not necessarily belong here, but main and test-extension have a lot of existing
 * config that would need to be duplicated, so it is here out of convenience.
 */
@QuarkusTest
class PrometheusMetricsRegistryTest {

    @Test
    void testRegistryInjection() {
        given()
                .when().get("/message")
                .then()
                .statusCode(200)
                .body(containsString("io.micrometer.prometheus.PrometheusMeterRegistry"));
    }

    @Test
    void testPrometheusScrapeEndpoint() {
        given()
                .when().get("/prometheus")
                .then()
                .statusCode(200)
                .body(containsString("jvm_classes_loaded_classes"));
    }
}
