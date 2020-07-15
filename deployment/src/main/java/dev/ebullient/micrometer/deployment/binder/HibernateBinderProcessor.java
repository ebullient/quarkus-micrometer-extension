package dev.ebullient.micrometer.deployment.binder;

import dev.ebullient.micrometer.runtime.binder.HibernateMetricsRecorder;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;

public class HibernateBinderProcessor {
    @BuildStep(onlyIf = VertxBinderProcessor.VertxBinderEnabled.class)
    @Record(value = ExecutionTime.RUNTIME_INIT)
    void createHibernateMicrometerBinders(Capabilities capabilities,
            BeanContainerBuildItem beanContainer,
            HibernateMetricsRecorder recorder) {

        if (!capabilities.isCapabilityPresent(Capabilities.HIBERNATE_ORM)) {
            return;
        }

        recorder.registerMetrics(beanContainer.getValue());
    }
}
