package dev.ebullient.micrometer.deployment.binder;

import dev.ebullient.micrometer.runtime.binder.HibernateMetricsProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

public class HibernateBinderProcessor {
    @BuildStep
    void createHibernateMicrometerBinders(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            Capabilities capabilities) {
        if (!capabilities.isCapabilityPresent(Capabilities.HIBERNATE_ORM)) {
            return;
        }

        additionalBeans.produce(
                AdditionalBeanBuildItem.builder()
                        .addBeanClass(HibernateMetricsProvider.class)
                        .setUnremovable()
                        .build());
    }
}
