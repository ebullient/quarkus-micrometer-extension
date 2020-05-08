package dev.ebullient.it.micrometer.prometheus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Test functioning prometheus endpoint.
 * Use test execution order to ensure one http server request measurement
 * is present when the endpoint is scraped.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PrometheusMetricsRegistryTest {

    @Test
    @Order(1)
    void testRegistryInjection() {
        given()
                .when().get("/message")
                .then()
                .statusCode(200)
                .body(containsString("io.micrometer.core.instrument.composite.CompositeMeterRegistry"));
    }

    @Test
    @Order(2)
    void testPrometheusScrapeEndpoint() {
        given()
                .when().get("/prometheus")
                .then()
                .log().body()
                .statusCode(200)
                .body(containsString("registry=\"prometheus\""))
                .body(containsString("env=\"test\""))
                .body(containsString("http_server_requests"));
    }
}
