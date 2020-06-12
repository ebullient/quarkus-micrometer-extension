package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.function.BooleanSupplier;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import dev.ebullient.micrometer.runtime.config.MicrometerConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.gizmo.ClassOutput;

/**
 * The microprofile API must remain optional. Avoid importing classes
 * that import MP Metrics API classes in turn.
 */
public class MicroprofileMetricsProcessor {

    public static final DotName MP_METRICS_BINDER = DotName
            .createSimple("dev.ebullient.micrometer.runtime.binder.microprofile.MicroprofileMetricsBinder");

    // these are needed for determining whether a class is a REST endpoint or JAX-RS provider
    public static final DotName JAXRS_PATH = DotName.createSimple("javax.ws.rs.Path");
    public static final DotName REST_CONTROLLER = DotName
            .createSimple("org.springframework.web.bind.annotation.RestController");

    // Metrics
    static final DotName GAUGE = DotName.createSimple("org.eclipse.microprofile.metrics.Gauge");

    static class MicroprofileMetricsEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            // Require explicit config: Some extensions reference MP Metrics classes,
            // so we can't consult the application classpath for help
            return mConfig.binder.mpMetrics.getEnabled().orElse(false);
        }
    }

    static boolean isSingleInstance(ClassInfo classInfo) {
        BuiltinScope beanScope = BuiltinScope.from(classInfo);

        return classInfo.annotations().containsKey(REST_CONTROLLER) ||
                classInfo.annotations().containsKey(JAXRS_PATH) ||
                BuiltinScope.APPLICATION.equals(beanScope) ||
                BuiltinScope.SINGLETON.equals(beanScope);
    }

    static String dotSeparate(String... values) {
        StringBuilder b = new StringBuilder();
        for (String s : values) {
            if (b.length() > 0) {
                b.append('.');
            }
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
        }
        return b.toString();
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    IndexDependencyBuildItem addDependencies() {
        return new IndexDependencyBuildItem("org.eclipse.microprofile.metrics", "microprofile-metrics-api");
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    AdditionalBeanBuildItem registerBeanClasses() {
        // Use string class names: do not force-load a class that pulls in microprofile dependencies
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(MP_METRICS_BINDER.toString())
                .setUnremovable()
                .build();
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    void processAnnotatedGauges(BuildProducer<GeneratedBeanBuildItem> generatedBeans,
            CombinedIndexBuildItem indexBuildItem) {
        IndexView index = indexBuildItem.getIndex();
        ClassOutput classOutput = new GeneratedBeanGizmoAdaptor(generatedBeans);

        // Defer MP Metrics imports until we know MP Metrics support in this extension
        // has been enabled.

        GaugeAnnotationHandler.processAnnotatedGauges(index, classOutput);
    }
}
