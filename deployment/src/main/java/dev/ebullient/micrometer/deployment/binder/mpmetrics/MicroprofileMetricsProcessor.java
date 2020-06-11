package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.function.BooleanSupplier;

import io.quarkus.arc.processor.BuiltinScope;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.binder.microprofile.MicroprofileMetricsBinder;
import dev.ebullient.micrometer.runtime.config.MicrometerConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;

public class MicroprofileMetricsProcessor {

    static final DotName METRIC_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Metric");
    static final Class<?> METRIC_ANNOTATION_CLASS = MicrometerRecorder
        .getClassForName(METRIC_ANNOTATION.toString());

    static class MicroprofileMetricsEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            return METRIC_ANNOTATION_CLASS != null && mConfig.checkBinderEnabledWithDefault(mConfig.binder.mpMetrics);
        }
    }

    // these are needed for determining whether a class is a REST endpoint or JAX-RS provider
    public static final DotName JAXRS_PATH = DotName.createSimple("javax.ws.rs.Path");
    public static final DotName REST_CONTROLLER = DotName
            .createSimple("org.springframework.web.bind.annotation.RestController");
    public static final DotName JAXRS_PROVIDER = DotName.createSimple("javax.ws.rs.ext.Provider");

    // Annotations
    static final DotName CONCURRENT_GAUGE_ANNOTATION = DotName
            .createSimple("org.eclipse.microprofile.metrics.annotation.ConcurrentGauge");
    static final DotName COUNTED_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Counted");
    static final DotName GAUGE_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Gauge");
    static final DotName METERED_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Metered");
    static final DotName SIMPLY_TIMED_ANNOTATION = DotName
            .createSimple("org.eclipse.microprofile.metrics.annotation.SimplyTimed");
    static final DotName TIMED_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Timed");
    static final DotName REGISTRY_TYPE_ANNOTATION = DotName
            .createSimple("org.eclipse.microprofile.metrics.annotation.RegistryType");

    // Metrics
    static final DotName COUNTER = DotName.createSimple("org.eclipse.microprofile.metrics.Counter");
    static final DotName HISTOGRAM = DotName.createSimple("org.eclipse.microprofile.metrics.Histogram");
    static final DotName METER = DotName.createSimple("org.eclipse.microprofile.metrics.Meter");
    static final DotName SIMPLE_TIMER = DotName.createSimple("org.eclipse.microprofile.metrics.SimpleTimer");
    static final DotName TIMER = DotName.createSimple("org.eclipse.microprofile.metrics.Timer");
    private static final Logger log = Logger.getLogger(MicroprofileMetricsProcessor.class);

    static boolean isSingleInstance(ClassInfo classInfo) {
        BuiltinScope beanScope = BuiltinScope.from(classInfo);

        return classInfo.annotations().containsKey(REST_CONTROLLER) ||
                classInfo.annotations().containsKey(JAXRS_PATH) ||
                BuiltinScope.APPLICATION.equals(beanScope) ||
                BuiltinScope.SINGLETON.equals(beanScope);
    }

    static String dotSeparate(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    b.append('.');
                }
                b.append(Character.toLowerCase(ch));
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    AdditionalBeanBuildItem registerBeanClasses() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(MicroprofileMetricsBinder.class)
                .setUnremovable()
                .build();
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("org.eclipse.microprofile.metrics", "microprofile-metrics-api"));
    }
}
