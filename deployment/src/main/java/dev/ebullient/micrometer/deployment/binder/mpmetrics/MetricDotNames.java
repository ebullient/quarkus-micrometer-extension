package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.*;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.quarkus.arc.processor.BuiltinScope;

/**
 * The microprofile API must remain optional.
 *
 * Avoid importing classes that import MP Metrics API classes.
 */
public class MetricDotNames {

    // Use string class names: do not force-load a class that pulls in microprofile dependencies
    static final DotName MP_METRICS_BINDER = DotName
            .createSimple("dev.ebullient.micrometer.runtime.binder.microprofile.MicroprofileMetricsBinder");
    static final DotName CONCURRENT_GAUGE_ANNOTATION = DotName
            .createSimple("org.eclipse.microprofile.metrics.annotation.ConcurrentGauge");
    static final DotName COUNTED_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Counted");
    static final DotName GAUGE_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Gauge");
    static final DotName METERED_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Metered");
    static final DotName METRIC_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Metric");
    static final DotName SIMPLY_TIMED_ANNOTATION = DotName
            .createSimple("org.eclipse.microprofile.metrics.annotation.SimplyTimed");
    static final DotName TIMED_ANNOTATION = DotName.createSimple("org.eclipse.microprofile.metrics.annotation.Timed");

    static final Set<DotName> METRICS_ANNOTATIONS = new HashSet<>(Arrays.asList(
            CONCURRENT_GAUGE_ANNOTATION,
            COUNTED_ANNOTATION,
            GAUGE_ANNOTATION,
            METERED_ANNOTATION,
            SIMPLY_TIMED_ANNOTATION,
            TIMED_ANNOTATION));

    // these are needed for determining whether a class is a REST endpoint or JAX-RS provider
    static final DotName JAXRS_PATH = DotName.createSimple("javax.ws.rs.Path");
    static final DotName REST_CONTROLLER = DotName
            .createSimple("org.springframework.web.bind.annotation.RestController");

    static final DotName COUNTED_INTERCEPTOR = DotName
            .createSimple("dev.ebullient.micrometer.runtime.binder.microprofile.CountedInterceptor");

    static boolean containsMetricAnnotation(Map<DotName, List<AnnotationInstance>> annotations) {
        for (DotName name : METRICS_ANNOTATIONS) {
            if (annotations.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    static boolean isSingleInstance(ClassInfo classInfo) {
        BuiltinScope beanScope = BuiltinScope.from(classInfo);
        return classInfo.annotations().containsKey(REST_CONTROLLER) ||
                classInfo.annotations().containsKey(JAXRS_PATH) ||
                BuiltinScope.APPLICATION.equals(beanScope) ||
                BuiltinScope.SINGLETON.equals(beanScope);
    }
}
