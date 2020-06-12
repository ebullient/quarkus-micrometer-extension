package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.binder.microprofile.GaugeAdapter;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

/**
 * Create beans to handle <code>@Gauge</code> annotations.
 * This is a static utility class, it stores no state. It is ok to import and use
 * classes that reference MP Metrics classes.
 */
public class GaugeAnnotationHandler {
    private static final Logger log = Logger.getLogger(GaugeAnnotationHandler.class);

    static final DotName GAUGE_ANNOTATION = DotName.createSimple(Gauge.class.getName());

    /**
     * Given this Widget class:
     *
     * <pre>
     * public class Widget  {
     *     private LongAccumulator highestValue = new LongAccumulator(Long::max, 0);
     *
     *     // ... some other things that change the value in the accumulator ...
     *
     *     &#64;Gauge(name = "highestValue", unit = MetricUnits.NONE, description = "Highest observed value.")
     *     public Long highestValue() {
     *         return highestValue.get();
     *     }
     * }
     * <pre>
     *
     * This method will generate a GaugeAdapter to call the annotated method:
     *
     * <pre>
     * public class Widget_GaugeAdapter extends GaugeAdapter.GaugeAdapterImpl implements GaugeAdapter {
     *
     *     &#64;Inject
     *     Widget target;
     *
     *     public Widget_GaugeAdapter() {
     *         // name, description, and tags are created from annotation attributes
     *         // init is a method on the superclass
     *         super(name, description, tags);
     *     }
     *
     *     Number getValue() {
     *         return target.highestValue;
     *     }
     *
     *     Object getValue() {
     *         return getValue();
     *     }
     * }
     * </pre>
     */
    static void processAnnotatedGauges(IndexView index, ClassOutput classOutput) {

        final Class<?> gaugeAdapter = GaugeAdapter.class;
        final Class<?> gaugeAdapterImpl = GaugeAdapter.GaugeAdapterImpl.class;

        final MethodDescriptor superInit = MethodDescriptor.ofConstructor(gaugeAdapterImpl, String.class, String.class,
                String[].class);

        // @Gauge applies to methods
        // It creates a callback the method or field on single object instance
        for (AnnotationInstance annotation : index.getAnnotations(GAUGE_ANNOTATION)) {
            AnnotationTarget target = annotation.target();
            MethodInfo method = target.asMethod();
            ClassInfo classInfo = method.declaringClass();

            // Annotated Gauges can only be used on single-instance beans
            verifyGaugeScope(target, classInfo);

            // Create a GaugeAdapter bean that uses the instance of the bean and invokes the callback
            final String generatedClassName = classInfo.name().toString() + "_GaugeAdapter";
            try (ClassCreator classCreator = ClassCreator.builder().classOutput(classOutput)
                    .className(generatedClassName)
                    .superClass(gaugeAdapterImpl)
                    .interfaces(gaugeAdapter)
                    .build()) {
                if (classInfo.annotations().containsKey("Singleton")) {
                    classCreator.addAnnotation(Singleton.class);
                } else {
                    classCreator.addAnnotation(ApplicationScoped.class);
                }

                GaugeAnnotationInfo gaugeInfo = new GaugeAnnotationInfo(classInfo, method, annotation, index);

                FieldCreator fieldCreator = classCreator.getFieldCreator("target", classInfo.name().toString())
                        .setModifiers(Modifier.PRIVATE | Modifier.FINAL);
                fieldCreator.addAnnotation(Inject.class);

                // Create the constructor
                try (MethodCreator mc = classCreator.getMethodCreator("<init>", void.class)) {
                    mc.setModifiers(Modifier.PUBLIC);

                    ResultHandle tagsHandle = mc.newArray(String.class, gaugeInfo.tags.size());
                    for (int i = 0; i < gaugeInfo.tags.size(); i++) {
                        mc.writeArrayValue(tagsHandle, i, mc.load(gaugeInfo.tags.get(i)));
                    }
                    // super(name, description, tags)
                    mc.invokeSpecialMethod(superInit, mc.getThis(),
                            mc.load(gaugeInfo.name),
                            mc.load(gaugeInfo.description),
                            tagsHandle);
                    mc.returnValue(null);
                }

                MethodDescriptor getNumberValue = null;
                try (MethodCreator mc = classCreator.getMethodCreator("getValue", Number.class)) {
                    mc.setModifiers(Modifier.PUBLIC);
                    ResultHandle targetInstance = mc.readInstanceField(fieldCreator.getFieldDescriptor(), mc.getThis());
                    mc.returnValue(mc.invokeVirtualMethod(target.asMethod(), targetInstance));
                    getNumberValue = mc.getMethodDescriptor();
                }

                try (MethodCreator generic = classCreator.getMethodCreator("getValue", Object.class)) {
                    generic.setModifiers(Modifier.PUBLIC);
                    generic.returnValue(generic.invokeVirtualMethod(getNumberValue, generic.getThis()));
                    generic.getMethodDescriptor();
                }
            }
        }
    }

    static private void verifyGaugeScope(AnnotationTarget target, ClassInfo classInfo) {
        log.debugf("Gauge: %s, %s, %s", target, target.kind(), classInfo);
        if (!MicroprofileMetricsProcessor.isSingleInstance(classInfo)) {
            log.errorf("Bean %s declares a org.eclipse.microprofile.metrics.annotation.Gauge " +
                    "but is of a scope that may create multiple instances of a bean. " +
                    "@Gauge annotations establish a callback to a single instance. Only use " +
                    "the @Gauge annotation on @ApplicationScoped or @Singleton beans, " +
                    "or in JAX-RS endpoints.",
                    classInfo.name().toString());
            throw new DeploymentException(classInfo.name().toString() +
                    " uses a @Gauge annotation, but is not @ApplicationScoped, a @Singleton, or a REST endpoint");
        }
    }

    static class GaugeAnnotationInfo {
        String name;
        String description;
        List<String> tags;

        GaugeAnnotationInfo(ClassInfo classInfo, MethodInfo method, AnnotationInstance annotation, IndexView index) {
            tags = new ArrayList<>();
            tags.add("scope=application");
            tags.addAll(Arrays.asList(annotation.valueWithDefault(index, "tags").asStringArray()));

            // Assign a name to the gauge
            name = annotation.valueWithDefault(index, "name").asString();

            boolean absolute = annotation.valueWithDefault(index, "absolute").asBoolean();
            if (!absolute) {
                // Generate a name: micrometer conventions for dotted strings
                if (name.isEmpty()) {
                    name = MicroprofileMetricsProcessor.dotSeparate(classInfo.simpleName(), method.name());
                } else {
                    name = MicroprofileMetricsProcessor.dotSeparate(classInfo.simpleName(), name);
                }
                String unit = annotation.valueWithDefault(index, "unit").asString();
                if (!unit.isEmpty() && !unit.equalsIgnoreCase("none")) {
                    name = name + "." + unit;
                }
                log.infof("%s will be registered using name %s", annotation, name);
            }

            description = annotation.valueWithDefault(index, "description").asString();
            if (description.isEmpty()) {
                description = name;
            }
        }
    }
}
