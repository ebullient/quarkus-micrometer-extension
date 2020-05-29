package dev.ebullient.micrometer.deployment;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import dev.ebullient.micrometer.runtime.ClockProvider;
import dev.ebullient.micrometer.runtime.CompositeRegistryCreator;
import dev.ebullient.micrometer.runtime.MeterFilterConstraint;
import dev.ebullient.micrometer.runtime.MeterFilterConstraints;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.binder.JvmMetricsProvider;
import dev.ebullient.micrometer.runtime.binder.SystemMetricsProvider;
import dev.ebullient.micrometer.runtime.config.MicrometerConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;

public class MicrometerProcessor {
    private static final DotName METER_REGISTRY = DotName.createSimple(MeterRegistry.class.getName());
    private static final DotName METER_BINDER = DotName.createSimple(MeterBinder.class.getName());
    private static final DotName METER_FILTER = DotName.createSimple(MeterFilter.class.getName());
    private static final DotName NAMING_CONVENTION = DotName.createSimple(NamingConvention.class.getName());

    private static final String FEATURE = "micrometer";

    static class MicrometerEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            return mConfig.enabled;
        }
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    // @BuildStep(onlyIf = MicrometerEnabled.class)
    // public CapabilityBuildItem capability() {
    // return new CapabilityBuildItem(Capabilities.METRICS);
    // }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    void addMicrometerDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        // indexDependency.produce(new IndexDependencyBuildItem("io.micrometer",
        // "micrometer-core"));
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    UnremovableBeanBuildItem registerAdditionalBeans(CombinedIndexBuildItem indexBuildItem,
            BuildProducer<MicrometerRegistryProviderBuildItem> providerClasses,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Create and keep JVM/System MeterBinders
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .setUnremovable()
                .addBeanClass(ClockProvider.class)
                .addBeanClass(JvmMetricsProvider.class)
                .addBeanClass(SystemMetricsProvider.class)
                .addBeanClass(CompositeRegistryCreator.class)
                .addBeanClass(MeterFilterConstraint.class)
                .addBeanClass(MeterFilterConstraints.class)
                .build());

        IndexView index = indexBuildItem.getIndex();

        // Find classes that define MeterRegistries, MeterBinders, and MeterFilters
        Collection<String> knownRegistries = new HashSet<>();
        collectNames(index.getAllKnownSubclasses(METER_REGISTRY), knownRegistries);

        Collection<String> knownClasses = new HashSet<>();
        knownClasses.add(METER_BINDER.toString());
        collectNames(index.getAllKnownImplementors(METER_BINDER), knownClasses);
        knownClasses.add(METER_FILTER.toString());
        collectNames(index.getAllKnownImplementors(METER_FILTER), knownClasses);
        knownClasses.add(NAMING_CONVENTION.toString());
        collectNames(index.getAllKnownImplementors(NAMING_CONVENTION), knownClasses);

        Set<String> keepMe = new HashSet<>();

        // Find and keep _producers_ of those MeterRegistries, MeterBinders, and
        // MeterFilters
        for (AnnotationInstance annotation : index.getAnnotations(DotNames.PRODUCES)) {
            AnnotationTarget target = annotation.target();
            switch (target.kind()) {
                case METHOD:
                    MethodInfo method = target.asMethod();
                    String returnType = method.returnType().name().toString();
                    if (knownRegistries.contains(returnType)) {
                        providerClasses.produce(new MicrometerRegistryProviderBuildItem(returnType));
                        keepMe.add(method.declaringClass().name().toString());
                    } else if (knownClasses.contains(returnType)) {
                        keepMe.add(method.declaringClass().name().toString());
                    }
                    break;
                case FIELD:
                    FieldInfo field = target.asField();
                    String fieldType = field.type().name().toString();
                    if (knownRegistries.contains(fieldType)) {
                        providerClasses.produce(new MicrometerRegistryProviderBuildItem(fieldType));
                        keepMe.add(field.declaringClass().name().toString());
                    } else if (knownClasses.contains(fieldType)) {
                        keepMe.add(field.declaringClass().name().toString());
                    }
                    break;
                default:
                    break;
            }
        }

        return new UnremovableBeanBuildItem(new UnremovableBeanBuildItem.BeanClassNamesExclusion(keepMe));
    }

    void collectNames(Collection<ClassInfo> classes, Collection<String> names) {
        for (ClassInfo info : classes) {
            names.add(info.name().toString());
        }
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    void configureRegistry(MicrometerRecorder recorder,
            List<MicrometerRegistryProviderBuildItem> providerClassItems,
            ShutdownContextBuildItem shutdownContextBuildItem) {

        Set<Class<? extends MeterRegistry>> typeClasses = new HashSet<>();
        for (MicrometerRegistryProviderBuildItem item : providerClassItems) {
            typeClasses.add(item.getRegistryClass());
        }
        recorder.configureRegistry(typeClasses, shutdownContextBuildItem);
    }

    public static boolean isInClasspath(String classname) {
        return MicrometerRecorder.getClassForName(classname) != null;
    }
}
