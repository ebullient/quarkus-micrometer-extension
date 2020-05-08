package dev.ebullient.micrometer.deployment.export;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.deployment.MicrometerBuildTimeConfig;
import dev.ebullient.micrometer.deployment.MicrometerRegistryProviderBuildItem;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.export.StackdriverMeterRegistryProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * Add support for the Stackdriver Meter Registry. Note that the registry may not
 * be available at deployment time for some projects: Avoid direct class
 * references.
 */
public class StackdriverRegistryProcessor {
    private static final Logger log = Logger.getLogger(StackdriverRegistryProcessor.class);

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
        StackdriverBuildTimeConfig config;

        public boolean getAsBoolean() {
            return REGISTRY_CLASS != null && mConfig.checkRegistryEnabledWithDefault(config.enabled);
        }
    }

    @BuildStep(onlyIf = { NativeBuild.class, StackdriverEnabled.class })
    MicrometerRegistryProviderBuildItem createStackdriverRegistry(CombinedIndexBuildItem index) {
        log.info("Stackdriver does not support running in native mode.");
        return null;
    }

    /** Stackdriver does not work with GraalVM */
    @BuildStep(onlyIf = StackdriverEnabled.class, onlyIfNot = NativeBuild.class, loadsApplicationClasses = true)
    MicrometerRegistryProviderBuildItem createStackdriverRegistry(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Add the Stackdriver Registry Producer
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(StackdriverMeterRegistryProvider.class)
                .setUnremovable().build());

        // Include the StackdriverMeterRegistry in a possible CompositeMeterRegistry
        return new MicrometerRegistryProviderBuildItem(REGISTRY_CLASS);
    }
}
