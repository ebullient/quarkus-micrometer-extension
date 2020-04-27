package dev.ebullient.micrometer.deployment.export;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.QuarkusUnitTest;

public class SecondPrometheusTest {
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(PrometheusRegistryProcessor.REGISTRY_CLASS)
                    .addClass(SecondPrometheusProvider.class)
                    .addAsResource(new StringAsset(
                            "quarkus.micrometer.export.prometheus.enabled=true\n"
                                    + "quarkus.micrometer.registry-enabled-default=false"),
                            "application.properties"));

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        // We want a composite that contains both registries.

    }
}
