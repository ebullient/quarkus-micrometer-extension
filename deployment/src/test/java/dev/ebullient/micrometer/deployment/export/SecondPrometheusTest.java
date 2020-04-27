package dev.ebullient.micrometer.deployment.export;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.test.QuarkusUnitTest;

public class SecondPrometheusTest {
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("test-logging.properties")
            .overrideConfigKey("quarkus.micrometer.export.prometheus.enabled", "true")
            .overrideConfigKey("quarkus.micrometer.registry-enabled-default", "false")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(PrometheusRegistryProcessor.REGISTRY_CLASS)
                    .addClass(SecondPrometheusProvider.class));

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        // We want a composite that contains both registries.
        Assertions.assertNotNull(registry, "A registry should be configured");
        Assertions.assertTrue(CompositeMeterRegistry.class.equals(registry.getClass()), "Should be CompositeMeterRegistry");
    }
}
