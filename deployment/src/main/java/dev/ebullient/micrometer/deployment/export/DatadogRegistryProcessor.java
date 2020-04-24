package dev.ebullient.micrometer.deployment.export;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.deployment.MicrometerBuildTimeConfig;
import dev.ebullient.micrometer.deployment.MicrometerProcessor;
import dev.ebullient.micrometer.deployment.MicrometerRegistryProviderBuildItem;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.export.DatadogMeterRegistryProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * Add support for the Datadog Meter Registry. Note that the registry may not
 * be available at deployment time for some projects: Avoid direct class
 * references.
 */
public class DatadogRegistryProcessor {
    private static final Logger log = Logger.getLogger(DatadogRegistryProcessor.class);

    static final String REGISTRY_CLASS_NAME = "io.micrometer.datadog.DatadogMeterRegistry";
    static final Class<?> REGISTRY_CLASS = MicrometerRecorder.getClassForName(REGISTRY_CLASS_NAME);

    @ConfigRoot(name = "micrometer.export.datadog", phase = ConfigPhase.BUILD_TIME)
    static class DatadogBuildTimeConfig {
        /**
         * If the Datadog micrometer registry is enabled.
         */
        @ConfigItem
        Optional<Boolean> enabled;

        @Override
        public String toString() {
            return this.getClass().getSimpleName()
                    + "{enabled=" + enabled
                    + '}';
        }
    }

    static class DatadogEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;
        DatadogBuildTimeConfig config;

        public boolean getAsBoolean() {
            boolean enabled = false;
            // TODO: Can't yet check for classes on the classpath in supplier
            //if (MicrometerProcessor.isInClasspath(REGISTRY_CLASS_NAME)) {
            enabled = mConfig.checkEnabledWithDefault(config.enabled);
            //}
            return enabled;
        }
    }

    /** Datadog does not work with GraalVM */
    @BuildStep(onlyIf = DatadogEnabled.class, loadsApplicationClasses = true)
    MicrometerRegistryProviderBuildItem createDatadogRegistry(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // TODO: remove this when the onlyIf check can do this
        // Double check that Datadog registry is on the classpath
        if (!MicrometerProcessor.isInClasspath(REGISTRY_CLASS_NAME)) {
            return null;
        }

        // Add the Datadog Registry Producer
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(DatadogMeterRegistryProvider.class)
                .setUnremovable().build());

        // Include the DatadogMeterRegistry in a possible CompositeMeterRegistry
        return new MicrometerRegistryProviderBuildItem(REGISTRY_CLASS);
    }
}
