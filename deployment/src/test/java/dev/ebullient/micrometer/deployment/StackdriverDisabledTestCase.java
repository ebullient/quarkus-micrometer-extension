package dev.ebullient.micrometer.deployment;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Should not have any registered MeterRegistry objects when micrometer is disabled
 */
public class StackdriverDisabledTestCase {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.micrometer.export.stackdriver.enabled=false"),
                            "application.properties"));

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        // Micrometer is enabled, stackdriver is not.
        Assertions.assertNotNull(registry, "A registry should be configured");
        Assertions.assertFalse(registry instanceof StackdriverMeterRegistry, "Should not be StackdriverMeterRegistry");
    }

}
