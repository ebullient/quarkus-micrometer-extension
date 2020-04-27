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
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.ClockProvider;
import dev.ebullient.micrometer.runtime.CompositeRegistryCreator;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.binder.JvmMetricsProvider;
import dev.ebullient.micrometer.runtime.binder.SystemMetricsProvider;
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
    private static final Logger log = Logger.getLogger(MicrometerProcessor.class);
    private static final DotName METER_REGISTRY = DotName.createSimple(MeterRegistry.class.getName());
    private static final DotName METER_BINDER = DotName.createSimple(MeterBinder.class.getName());
    private static final DotName METER_FILTER = DotName.createSimple(MeterFilter.class.getName());
    private static final DotName NAMING_CONVENTION = DotName.createSimple(NamingConvention.class.getName());

    private static final String FEATURE = "micrometer";

    static class MicrometerEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;

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
        additionalBeans.produce(AdditionalBeanBuildItem.builder().setUnremovable().addBeanClass(ClockProvider.class)
                .addBeanClass(JvmMetricsProvider.class).addBeanClass(SystemMetricsProvider.class).build());

        IndexView index = indexBuildItem.getIndex();

        // Find classes that define MeterRegistries, MeterBinders, and MeterFilters
        Collection<ClassInfo> knownRegistries = index.getAllKnownSubclasses(METER_REGISTRY);
        Collection<ClassInfo> knownClasses = new HashSet<ClassInfo>();
        knownClasses.addAll(index.getAllKnownImplementors(METER_BINDER));
        knownClasses.addAll(index.getAllKnownImplementors(METER_FILTER));
        knownClasses.addAll(index.getAllKnownImplementors(NAMING_CONVENTION));

        Set<DotName> keepMe = new HashSet<>();

        // Find and keep _producers_ of those MeterRegistries, MeterBinders, and
        // MeterFilters
        for (AnnotationInstance annotation : index.getAnnotations(DotNames.PRODUCES)) {
            AnnotationTarget target = annotation.target();
            ClassInfo type;
            switch (target.kind()) {
                case METHOD:
                    MethodInfo method = target.asMethod();
                    type = index.getClassByName(method.returnType().name());
                    if (knownRegistries.contains(type)) {
                        providerClasses.produce(new MicrometerRegistryProviderBuildItem(type));
                        keepMe.add(method.declaringClass().name());
                    } else if (knownClasses.contains(type)) {
                        keepMe.add(method.declaringClass().name());
                    }
                    break;
                case FIELD:
                    FieldInfo field = target.asField();
                    type = index.getClassByName(field.type().name());
                    if (knownRegistries.contains(type)) {
                        providerClasses.produce(new MicrometerRegistryProviderBuildItem(type));
                        keepMe.add(field.declaringClass().name());
                    } else if (knownClasses.contains(type)) {
                        keepMe.add(field.declaringClass().name());
                    }
                    break;
                default:
                    break;
            }
        }

        return new UnremovableBeanBuildItem(new UnremovableBeanBuildItem.BeanTypesExclusion(keepMe));
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    void createRootRegistry(
            BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItem) {
        additionalBeanBuildItem.produce(AdditionalBeanBuildItem.builder().addBeanClass(CompositeRegistryCreator.class).build());
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    void configureRegistry(MicrometerRecorder recorder,
            List<MicrometerRegistryProviderBuildItem> providerClasses,
            ShutdownContextBuildItem shutdownContextBuildItem) {

        recorder.configureRegistry(shutdownContextBuildItem);
    }

    public static boolean isInClasspath(String classname) {
        log.debug("findClass TCCL: " + Thread.currentThread().getContextClassLoader() + " ## " + classname);
        try {
            Class.forName(classname, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            log.debug("findClass TCCL: " + Thread.currentThread().getContextClassLoader() + " ## " + classname
                    + ": false");
            return false;
        }
    }

    public static Class<?> getClass(String classname) {
        log.debug("findClass TCCL: " + Thread.currentThread().getContextClassLoader() + " ## " + classname);
        try {
            return Class.forName(classname, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
