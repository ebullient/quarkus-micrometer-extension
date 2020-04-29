package dev.ebullient.micrometer.deployment.export;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.deployment.MicrometerBuildTimeConfig;
import dev.ebullient.micrometer.deployment.MicrometerRegistryProviderBuildItem;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.export.JmxMeterRegistryProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * Add support for the Jmx Meter Registry. Note that the registry may not
 * be available at deployment time for some projects: Avoid direct class
 * references.
 */
public class JmxRegistryProcessor {
    private static final Logger log = Logger.getLogger(JmxRegistryProcessor.class);

    static final String REGISTRY_CLASS_NAME = "io.micrometer.jmx.JmxMeterRegistry";
    static final Class<?> REGISTRY_CLASS = MicrometerRecorder.getClassForName(REGISTRY_CLASS_NAME);

    @ConfigRoot(name = "micrometer.export.jmx", phase = ConfigPhase.BUILD_TIME)
    static class JmxBuildTimeConfig {
        /**
         * If the Jmx micrometer registry is enabled.
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

    static class JmxEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;
        JmxBuildTimeConfig config;

        public boolean getAsBoolean() {
            return REGISTRY_CLASS != null && mConfig.checkEnabledWithDefault(config.enabled);
        }
    }

    @BuildStep(onlyIf = { NativeBuild.class, JmxEnabled.class })
    MicrometerRegistryProviderBuildItem createJmxRegistry(CombinedIndexBuildItem index) {
        log.info("JMX Meter Registry does not support running in native mode.");
        return null;
    }

    /** Jmx does not work with GraalVM */
    @BuildStep(onlyIf = JmxEnabled.class, onlyIfNot = NativeBuild.class, loadsApplicationClasses = true)
    MicrometerRegistryProviderBuildItem createJmxRegistry(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Add the Jmx Registry Producer
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(JmxMeterRegistryProvider.class)
                .setUnremovable().build());

        // Include the JmxMeterRegistry in a possible CompositeMeterRegistry
        return new MicrometerRegistryProviderBuildItem(REGISTRY_CLASS);
    }
}
