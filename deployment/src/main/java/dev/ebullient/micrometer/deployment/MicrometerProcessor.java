package dev.ebullient.micrometer.deployment;

import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.ClockProvider;
import dev.ebullient.micrometer.runtime.CompositeMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.NoopMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.PrometheusMeterRegistryProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class MicrometerProcessor {
    private static final Logger LOGGER = Logger.getLogger(MicrometerProcessor.class.getName());
    private static final String FEATURE = "micrometer";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    // @BuildStep
    // public CapabilityBuildItem capability() {
    // return new CapabilityBuildItem(Capabilities.METRICS);
    // }

    @BuildStep
    void registerAdditionalBeans(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<UnremovableBeanBuildItem> unremovableBean) {

        // CDI Provider that will create/inject default No-Op Micrometer instance
        additionalBeans.produce(new AdditionalBeanBuildItem(ClockProvider.class));

        boolean useNoopRegistry = true;

        if (isInClasspath(MicrometerDotNames.PROMETHEUS_REGISTRY)) {
            useNoopRegistry = false;
            // CDI Provider that will create/inject default Micrometer instances
            //additionalBeans.produce(new AdditionalBeanBuildItem(MicrometerProvider.class));
            System.out.println("PROM ON THE CLASSPATH");
            additionalBeans.produce(new AdditionalBeanBuildItem(PrometheusMeterRegistryProvider.class));
        }

        if (useNoopRegistry) {
            // CDI Provider that will create/inject default No-Op Micrometer instance
            additionalBeans.produce(new AdditionalBeanBuildItem(NoopMeterRegistryProvider.class));
        } else {
            additionalBeans.produce(new AdditionalBeanBuildItem(CompositeMeterRegistryProvider.class));
        }
    }

    private static boolean isInClasspath(DotName classname) {
        try {
            Class.forName(classname.toString());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
