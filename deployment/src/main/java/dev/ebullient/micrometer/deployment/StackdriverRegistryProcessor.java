package dev.ebullient.micrometer.deployment;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.StackdriverMeterRegistryProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * Add support for the Stackdriver Meter Registry. Note that the registry may not
 * be available at deployment time for some projects: Avoid direct class
 * references.
 */
public class StackdriverRegistryProcessor {
    static final String REGISTRY_CLASS_NAME = "io.micrometer.stackdriver.StackdriverMeterRegistry";
    static final Class<?> REGISTRY_CLASS = MicrometerRecorder.getClassForName(REGISTRY_CLASS_NAME);

    @ConfigRoot(name = "micrometer.export.stackdriver", phase = ConfigPhase.BUILD_TIME)
    static class StackdriverBuildTimeConfig {
        /**
         * If the Stackdriver micrometer registry is enabled.
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

    static class StackdriverEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;
        StackdriverBuildTimeConfig sConfig;

        public boolean getAsBoolean() {
            boolean enabled = false;
            // TODO: Can't yet check for classes on the classpath in supplier
            //if (MicrometerProcessor.isInClasspath(REGISTRY_CLASS_NAME)) {
            enabled = mConfig.checkEnabledWithDefault(sConfig.enabled);
            //}
            return enabled;
        }
    }

    @BuildStep(onlyIf = StackdriverEnabled.class, loadsApplicationClasses = true)
    MicrometerRegistryProviderBuildItem createStackdriverRegistry(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // TODO: remove this when the onlyIf check can do this
        // Double check that Stackdriver registry is on the classpath
        if (!MicrometerProcessor.isInClasspath(REGISTRY_CLASS_NAME)) {
            return null;
        }

        // Add the Stackdriver Registry Producer
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(StackdriverMeterRegistryProvider.class)
                .setUnremovable().build());

        // Include the StackdriverMeterRegistry in a possible CompositeMeterRegistry
        return new MicrometerRegistryProviderBuildItem(REGISTRY_CLASS);
    }
}
