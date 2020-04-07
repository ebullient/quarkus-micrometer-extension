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

public class StackdriverEnabledTestCase {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.micrometer.export.stackdriver.enabled=true\n"
                            + "quarkus.micrometer.export.stackdriver.project-id=myproject"),
                            "application.properties"));

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        // Stackdriver is enabled.
        Assertions.assertNotNull(registry, "A registry should be configured");
        Assertions.assertTrue(registry instanceof StackdriverMeterRegistry, "Should be StackdriverMeterRegistry");
    }

}
