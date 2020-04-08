package dev.ebullient.micrometer.deployment;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.quarkus.test.QuarkusUnitTest;

public class PrometheusEnabledTestCase {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(PrometheusMeterRegistry.class)
                    .addAsResource(new StringAsset(
                            "quarkus.micrometer.export.prometheus.enabled=true\n"
                                    + "quarkus.micrometer.registry-enabled-default=false"),
                            "application.properties"));

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        // Prometheus is enabled (only registry)
        Assertions.assertNotNull(registry, "A registry should be configured");
        Assertions.assertTrue(registry instanceof PrometheusMeterRegistry, "Should be PrometheusMeterRegistry");
    }
}
