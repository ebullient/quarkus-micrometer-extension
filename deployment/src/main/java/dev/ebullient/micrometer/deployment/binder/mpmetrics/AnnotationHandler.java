package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.Collection;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.DotNames;

/**
 * Create beans to handle <code>@Counted</code> annotations.
 *
 * It is ok to import and use classes that reference MP Metrics classes.
 *
 * A counter will be created for methods (or constructors) annotated with {@literal @}Counted.
 * Each time the method is invoked, the counter will be incremented.
 *
 * If a class is annotated with {@literal @}Counted, counters will be created for each method
 * and the constructor. Each time a method is invoked, the related counter will be incremented.
 */
public class AnnotationHandler {
    private static final Logger log = Logger.getLogger(AnnotationHandler.class);

    static AnnotationsTransformerBuildItem processCountedAnnotations(final IndexView index) {
        // @Counted applies to classes and methods
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
            @Override
            public void transform(TransformationContext ctx) {
                final Collection<AnnotationInstance> annotations = ctx.getAnnotations();
                AnnotationInstance annotation = getMeterAnnotations(annotations, MetricDotNames.COUNTED_ANNOTATION);
                if (annotation == null) {
                    return;
                }
                AnnotationTarget target = ctx.getTarget();

                ClassInfo classInfo = null;
                MethodInfo methodInfo = null;
                if (ctx.isMethod()) {
                    methodInfo = target.asMethod();
                    classInfo = methodInfo.declaringClass();
                } else if (ctx.isClass()) {
                    if (target.asClass().classAnnotation(DotNames.INTERCEPTOR) != null) {
                        // skip @Counted @Interceptor
                        return;
                    }
                    classInfo = target.asClass();
                }
                log.debugf("## COUNTED: %s", annotation.values());

                // Make sure all attributes exist on the @Counted annotation
                // remove the existing annotation, and add a new one with all the fields
                MetricAnnotationInfo counterInfo = new MetricAnnotationInfo(annotation, index, classInfo, methodInfo);
                ctx.transform()
                        .remove(x -> x == annotation)
                        .add(MetricDotNames.COUNTED_ANNOTATION, counterInfo.getAnnotationValues())
                        .done();

            }
        });
    }

    static AnnotationsTransformerBuildItem processTimedAnnotations(final IndexView index) {
        // @Timed applies to classes and methods
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
            @Override
            public void transform(TransformationContext ctx) {
                final Collection<AnnotationInstance> annotations = ctx.getAnnotations();
                AnnotationInstance annotation = getMeterAnnotations(annotations, MetricDotNames.TIMED_ANNOTATION);
                if (annotation == null) {
                    return;
                }
                AnnotationTarget target = ctx.getTarget();

                ClassInfo classInfo = null;
                MethodInfo methodInfo = null;
                if (ctx.isMethod()) {
                    methodInfo = target.asMethod();
                    classInfo = methodInfo.declaringClass();
                } else if (ctx.isClass()) {
                    if (target.asClass().classAnnotation(DotNames.INTERCEPTOR) != null) {
                        // skip @Timed @Interceptor
                        return;
                    }
                    classInfo = target.asClass();
                }
                log.debugf("## TIMED: %s", annotation.values());

                // Make sure all attributes exist on the @Timed annotation
                // remove the existing annotation, and add a new one with all the fields
                MetricAnnotationInfo timedInfo = new MetricAnnotationInfo(annotation, index, classInfo, methodInfo);
                ctx.transform()
                        .remove(x -> x == annotation)
                        .add(MetricDotNames.TIMED_ANNOTATION, timedInfo.getAnnotationValues())
                        .done();

            }
        });
    }

    private static AnnotationInstance getMeterAnnotations(Collection<AnnotationInstance> annotations, DotName annotationClass) {
        for (AnnotationInstance a : annotations) {
            if (annotationClass.equals(a.name())) {
                return a;
            }
        }
        return null;
    }
}
