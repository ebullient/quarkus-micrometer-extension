package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.Collection;

import org.jboss.jandex.*;
import org.jboss.logging.Logger;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.DotNames;

/**
 * Create beans to handle MP Metrics API annotations.
 *
 * It is ok to import and use classes that reference MP Metrics classes.
 */
public class AnnotationHandler {
    private static final Logger log = Logger.getLogger(AnnotationHandler.class);

    static AnnotationsTransformerBuildItem transformAnnotations(final IndexView index, DotName meterAnnotation) {
        return transformAnnotations(index, meterAnnotation, meterAnnotation);
    }

    static AnnotationsTransformerBuildItem transformAnnotations(final IndexView index,
            DotName sourceAnnotation, DotName targetAnnotation) {
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
            @Override
            public void transform(TransformationContext ctx) {
                final Collection<AnnotationInstance> annotations = ctx.getAnnotations();
                AnnotationInstance annotation = findAnnotation(annotations, sourceAnnotation);
                if (annotation == null) {
                    return;
                }
                AnnotationTarget target = ctx.getTarget();

                ClassInfo classInfo = null;
                MethodInfo methodInfo = null;
                FieldInfo fieldInfo = null;
                if (ctx.isMethod()) {
                    methodInfo = target.asMethod();
                    classInfo = methodInfo.declaringClass();
                } else if (ctx.isField()) {
                    fieldInfo = target.asField();
                    classInfo = fieldInfo.declaringClass();
                } else if (ctx.isClass()) {
                    classInfo = target.asClass();
                    // skip @Interceptor
                    if (target.asClass().classAnnotation(DotNames.INTERCEPTOR) != null) {
                        return;
                    }
                }

                // Remove the @Counted annotation (avoid interceptor) for
                // things that are both counted AND timed
                if (MetricDotNames.COUNTED_ANNOTATION.equals(sourceAnnotation) &&
                        removeCountedWhenTimed(target, classInfo, methodInfo)) {
                    ctx.transform()
                            .remove(x -> x == annotation)
                            .done();
                    return;
                }

                // Make sure all attributes exist:
                MetricAnnotationInfo annotationInfo = new MetricAnnotationInfo(annotation, index,
                        classInfo, methodInfo, fieldInfo);

                // Remove the existing annotation, and add a new one with all the fields
                ctx.transform()
                        .remove(x -> x == annotation)
                        .add(targetAnnotation, annotationInfo.getAnnotationValues())
                        .done();
            }
        });
    }

    public static AnnotationsTransformerBuildItem transformMetricAnnotations(IndexView index) {
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
            @Override
            public void transform(TransformationContext ctx) {
                final Collection<AnnotationInstance> annotations = ctx.getAnnotations();
                AnnotationInstance annotation = findAnnotation(annotations, MetricDotNames.METRIC_ANNOTATION);
                if (annotation == null) {
                    return;
                }
                AnnotationTarget target = ctx.getTarget();

                if (findAnnotation(annotations, DotNames.PRODUCES) != null) {
                    log.errorf("A declared bean uses the @Metric annotation with a @Producer " +
                            "field or method, which is not compatible with micrometer support. " +
                            "The annotation target is %s",
                            ctx.getTarget());
                    ctx.transform()
                            .remove(x -> x == annotation)
                            .done();
                }

                ClassInfo classInfo = null;
                MethodInfo methodInfo = null;
                FieldInfo fieldInfo = null;
                if (ctx.isMethod()) {
                    methodInfo = target.asMethod();
                    classInfo = methodInfo.declaringClass();
                } else if (ctx.isField()) {
                    fieldInfo = target.asField();
                    classInfo = fieldInfo.declaringClass();
                } else if (ctx.isClass()) {
                    classInfo = target.asClass();
                    // skip @Interceptor
                    if (target.asClass().classAnnotation(DotNames.INTERCEPTOR) != null) {
                        return;
                    }
                }

                // Make sure all attributes exist:
                MetricAnnotationInfo annotationInfo = new MetricAnnotationInfo(annotation, index,
                        classInfo, methodInfo, fieldInfo);

                ctx.transform()
                        .remove(x -> x == annotation)
                        .add(MetricDotNames.METRIC_ANNOTATION, annotationInfo.getAnnotationValues())
                        .done();
            }
        });
    }

    private static AnnotationInstance findAnnotation(Collection<AnnotationInstance> annotations, DotName annotationClass) {
        for (AnnotationInstance a : annotations) {
            if (annotationClass.equals(a.name())) {
                return a;
            }
        }
        return null;
    }

    static boolean removeCountedWhenTimed(AnnotationTarget target, ClassInfo classInfo, MethodInfo methodInfo) {
        if (methodInfo == null &&
                findAnnotation(classInfo.classAnnotations(), MetricDotNames.TIMED_ANNOTATION) == null &&
                findAnnotation(classInfo.classAnnotations(), MetricDotNames.SIMPLY_TIMED_ANNOTATION) == null) {
            return false;
        }
        if (!methodInfo.hasAnnotation(MetricDotNames.SIMPLY_TIMED_ANNOTATION) &&
                !methodInfo.hasAnnotation(MetricDotNames.TIMED_ANNOTATION)) {
            return false;
        }

        if (methodInfo == null) {
            log.warnf("Bean %s is both counted and timed. The @Counted annotation " +
                    "will be suppressed in favor of the count emitted by the timer.",
                    classInfo.name().toString());
        } else {
            log.warnf("Method %s of bean %s is both counted and timed. The @Counted annotation " +
                    "will be suppressed in favor of the count emitted by the timer.",
                    methodInfo.name(),
                    classInfo.name().toString());
        }
        return true;
    }
}
