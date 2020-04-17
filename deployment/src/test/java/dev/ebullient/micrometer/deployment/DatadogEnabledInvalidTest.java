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
import io.micrometer.core.instrument.config.MissingRequiredConfigurationException;
import io.quarkus.test.QuarkusUnitTest;

public class DatadogEnabledInvalidTest {
    static final String REGISTRY_CLASS_NAME = "io.micrometer.datadog.DatadogMeterRegistry";
    static final Class<?> REGISTRY_CLASS = MicrometerRecorder.getClassForName(REGISTRY_CLASS_NAME);

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(DatadogRegistryProcessor.REGISTRY_CLASS)
                    .addAsResource(new StringAsset(
                            "quarkus.micrometer.export.datadog.enabled=true\n"
                                    + "quarkus.micrometer.registry-enabled-default=false\n"),
                            "application.properties"))
            .assertException(t -> {
                Assertions.assertEquals(MissingRequiredConfigurationException.class.getName(), t.getClass().getName());
            });

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        Assertions.fail("Runtime should not have initialized with missing apiKey");
    }
}
