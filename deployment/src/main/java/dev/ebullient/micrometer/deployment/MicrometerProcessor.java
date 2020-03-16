package dev.ebullient.micrometer.deployment;

import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
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
    //     return new CapabilityBuildItem(Capabilities.METRICS);
    // }

    @BuildStep
    void build(CombinedIndexBuildItem combinedIndexBuildItem,
            BuildProducer<GeneratedBeanBuildItem> generatedBeans) {

        System.out.println("I AM HERE");

        ClassLoader classloader = MeterRegistry.class.getClassLoader();
        Indexer indexer = new Indexer();
        Set<DotName> additionalIndex = new HashSet<>();

        // CompositeIndex compositeIndex = CompositeIndex.create(combinedIndexBuildItem, indexer.complete());
    }

}
