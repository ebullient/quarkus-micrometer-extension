package dev.ebullient.micrometer.deployment;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.QuarkusUnitTest;

public class JmxEnabledTest {
    static final String REGISTRY_CLASS_NAME = "io.micrometer.jmx.JmxMeterRegistry";
    static final Class<?> REGISTRY_CLASS = MicrometerRecorder.getClassForName(REGISTRY_CLASS_NAME);

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(JmxRegistryProcessor.REGISTRY_CLASS)
                    .addAsResource(new StringAsset(
                            "quarkus.micrometer.export.jmx.enabled=true\n"
                                    + "quarkus.micrometer.registry-enabled-default=false\n"),
                            "application.properties"));

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        // Jmx is enabled (alone, all others disabled)
        Assertions.assertNotNull(registry, "A registry should be configured");
        Assertions.assertTrue(REGISTRY_CLASS.equals(registry.getClass()), "Should be JmxMeterRegistry");
    }
}
