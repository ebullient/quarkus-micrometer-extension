package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.function.BooleanSupplier;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.config.MicrometerConfig;
import io.quarkus.arc.deployment.*;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BuildExtension;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.gizmo.ClassOutput;

/**
 * The microprofile API must remain optional.
 *
 * Avoid importing classes that import MP Metrics API classes.
 */
public class MicroprofileMetricsProcessor {
    private static final Logger log = Logger.getLogger(MicroprofileMetricsProcessor.class);

    static class MicroprofileMetricsEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            // Require explicit config: Some extensions reference MP Metrics classes,
            // so we can't consult the application classpath for help
            return mConfig.binder.mpMetrics.getEnabled().orElse(false);
        }
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    IndexDependencyBuildItem addDependencies() {
        return new IndexDependencyBuildItem("org.eclipse.microprofile.metrics", "microprofile-metrics-api");
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    AdditionalBeanBuildItem registerBeanClasses() {
        return AdditionalBeanBuildItem.builder()
                .setUnremovable()
                .addBeanClass(MetricDotNames.MP_METRICS_BINDER.toString())
                .addBeanClass(MetricDotNames.COUNTED_INTERCEPTOR.toString())
                .build();
    }

    /**
     * Make sure all classes containing metrics annotations have a bean scope.
     */
    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    AnnotationsTransformerBuildItem transformBeanScope(BeanArchiveIndexBuildItem index,
            CustomScopeAnnotationsBuildItem scopes) {
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
            @Override
            public int getPriority() {
                // this specifically should run after the JAX-RS AnnotationTransformers
                return BuildExtension.DEFAULT_PRIORITY - 100;
            }

            @Override
            public boolean appliesTo(AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void transform(TransformationContext ctx) {
                if (scopes.isScopeIn(ctx.getAnnotations())) {
                    return;
                }
                ClassInfo clazz = ctx.getTarget().asClass();
                if (!MetricDotNames.isSingleInstance(clazz)) {
                    while (clazz != null && clazz.superName() != null) {
                        if (MetricDotNames.containsMetricAnnotation(clazz.annotations())) {
                            log.debugf(
                                    "Found metrics business methods on a class %s with no scope defined - adding @Dependent",
                                    ctx.getTarget());
                            ctx.transform().add(Dependent.class).done();
                            break;
                        }
                        clazz = index.getIndex().getClassByName(clazz.superName());
                    }
                }
            }
        });
    }

    @BuildStep(onlyIf = MicroprofileMetricsEnabled.class)
    void processAnnotatedMetrics(BuildProducer<GeneratedBeanBuildItem> generatedBeans,
            BuildProducer<AnnotationsTransformerBuildItem> annotationsTransformers,
            CombinedIndexBuildItem indexBuildItem) {
        IndexView index = indexBuildItem.getIndex();
        ClassOutput classOutput = new GeneratedBeanGizmoAdaptor(generatedBeans);

        // Use classes to defer MP Metrics imports until we know MP Metrics support
        // has been enabled.
        GaugeAnnotationHandler.processAnnotatedGauges(index, classOutput);
        annotationsTransformers.produce(AnnotationHandler.processCountedAnnotations(index));
    }
}
