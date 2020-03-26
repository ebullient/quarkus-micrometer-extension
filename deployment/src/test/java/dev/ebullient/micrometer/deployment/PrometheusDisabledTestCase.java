package dev.ebullient.micrometer.deployment;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

/**
 * Should not have any registered MeterRegistry objects when micrometer is disabled
 */
public class PrometheusDisabledTestCase {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.micrometer.export.prometheus.enabled=false"),
                            "application.properties"));

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        // Micrometer is enabled, prometheus is not.
        Assertions.assertNotNull(registry, "A registry should be configured");
    }

    @Test
    public void testNoPrometheusEndpoint() {
        // Micrometer is enabled, prometheus is not.
        RestAssured.when().get("/prometheus").then().statusCode(404);
    }
}
