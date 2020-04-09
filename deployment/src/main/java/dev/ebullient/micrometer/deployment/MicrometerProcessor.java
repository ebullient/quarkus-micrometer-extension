package dev.ebullient.micrometer.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor.Priority;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.ClockProvider;
import dev.ebullient.micrometer.runtime.JvmMetricsProvider;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.NoopMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.SystemMetricsProvider;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.AlternativePriority;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

class MicrometerProcessor {
    private static final Logger log = Logger.getLogger(MicrometerProcessor.class);

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
        // indexDependency.produce(new IndexDependencyBuildItem("io.micrometer", "micrometer-core"));
        // indexDependency.produce(new IndexDependencyBuildItem("io.micrometer", "micrometer-registry-prometheus"));
        // indexDependency.produce(new IndexDependencyBuildItem("io.micrometer", "micrometer-registry-stackdriver"));
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    void registerAdditionalBeans(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<MicrometerRegistryProviderBuildItem> additionalRegistryProviders) {

        // Create and keep JVM/System MeterBinders
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .setUnremovable()
                .addBeanClass(ClockProvider.class)
                .addBeanClass(JvmMetricsProvider.class)
                .addBeanClass(SystemMetricsProvider.class)
                .build());

        // Find customizers, binders, and custom provider registries
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    void createRootRegistry(List<MicrometerRegistryProviderBuildItem> providerClasses,
            BuildProducer<GeneratedBeanBuildItem> beanProducer,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        if (providerClasses.isEmpty()) {
            // No MeterRegistries found. Create a no-op Composite for CDI injection
            additionalBeans.produce(AdditionalBeanBuildItem.builder().setUnremovable()
                    .addBeanClass(NoopMeterRegistryProvider.class).build());
        } else if (providerClasses.size() > 1) {
            // Many MeterRegistries found
            // Create a CompositeMeterRegistry, and add enabled MeterRegistry beans
            GeneratedBeanGizmoAdaptor gizmoAdaptor = new GeneratedBeanGizmoAdaptor(beanProducer);

            String name = this.getClass().getPackage().getName() + ".CompositeMicrometerRegistryProvider";
            try (ClassCreator classCreator = ClassCreator.builder().className(name).classOutput(gizmoAdaptor).build()) {
                classCreator.addAnnotation(Singleton.class);

                List<FieldDescriptor> listFields = new ArrayList<>();

                int i = 0;
                // create fields to allow all other known/enabled registry beans to be injected
                for (MicrometerRegistryProviderBuildItem provider : providerClasses) {
                    FieldCreator injected = classCreator.getFieldCreator("registry_" + i,
                            provider.getProvidedRegistryClass().getName());
                    injected.addAnnotation(Inject.class);
                    injected.setModifiers(0);
                    listFields.add(injected.getFieldDescriptor());
                }

                // @Produces
                try (MethodCreator createCompositeRegistry = classCreator.getMethodCreator("createCompositeRegistry",
                        MeterRegistry.class, Clock.class)) {
                    createCompositeRegistry.addAnnotation(Produces.class);
                    createCompositeRegistry.addAnnotation(Singleton.class);

                    // The composite takes precedence over all registries
                    createCompositeRegistry.addAnnotation(AlternativePriority.class).addValue("value",
                            Priority.PLATFORM_AFTER);

                    // create the composite registry
                    ResultHandle compositeRegistry = createCompositeRegistry.newInstance(
                            MethodDescriptor.ofConstructor(CompositeMeterRegistry.class, Clock.class),
                            createCompositeRegistry.getMethodParam(0));

                    // add injected registries to the composite
                    MethodDescriptor addRegistry = MethodDescriptor.ofMethod(CompositeMeterRegistry.class, "add",
                            CompositeMeterRegistry.class, MeterRegistry.class);
                    for (FieldDescriptor fd : listFields) {
                        ResultHandle arg = createCompositeRegistry.readInstanceField(fd, createCompositeRegistry.getThis());
                        createCompositeRegistry.invokeVirtualMethod(addRegistry, compositeRegistry, arg);
                    }

                    // all done!
                    createCompositeRegistry.returnValue(compositeRegistry);
                }
            }
        }
    }

    @BuildStep(onlyIf = MicrometerEnabled.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    void configureRegistry(MicrometerRecorder recorder, ShutdownContextBuildItem shutdownContextBuildItem) {
        recorder.configureRegistry(shutdownContextBuildItem);
    }

    static boolean isInClasspath(String classname) {
        log.debug("findClass TCCL: " + Thread.currentThread().getContextClassLoader() + " ## " + classname);
        try {
            Class.forName(classname, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            log.debug("findClass TCCL: " + Thread.currentThread().getContextClassLoader() + " ## " + classname + ": false");
            return false;
        }
    }

}
