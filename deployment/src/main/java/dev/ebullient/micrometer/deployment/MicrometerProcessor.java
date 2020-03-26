package dev.ebullient.micrometer.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor.Priority;

import org.jboss.jandex.DotName;

import dev.ebullient.micrometer.runtime.ClockProvider;
import dev.ebullient.micrometer.runtime.JvmMetricsProvider;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.NoopMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.PrometheusMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.PrometheusScrapeHandler;
import dev.ebullient.micrometer.runtime.SystemMetricsProvider;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.quarkus.arc.AlternativePriority;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class MicrometerProcessor {
    private static final String FEATURE = "micrometer";

    MicrometerConfig mConfig;
    PrometheusConfig pConfig;

    @BuildStep(onlyIf = MicrometerConfig.MicrometerEnabled.class)
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    // @BuildStep(onlyIf = MicrometerEnabled.class)
    // public CapabilityBuildItem capability() {
    // return new CapabilityBuildItem(Capabilities.METRICS);
    // }

    @BuildStep(onlyIf = MicrometerConfig.MicrometerEnabled.class)
    void addMicrometerDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.micrometer", "micrometer-core"));
        indexDependency.produce(new IndexDependencyBuildItem("io.micrometer", "micrometer-registry-prometheus"));
    }

    @BuildStep(onlyIf = MicrometerConfig.MicrometerEnabled.class)
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

    @BuildStep(onlyIf = PrometheusConfig.PrometheusEnabled.class)
    MicrometerRegistryProviderBuildItem createPrometheusRegistry(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Add the Prometheus Registry Producer
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(PrometheusMeterRegistryProvider.class)
                .setUnremovable().build());

        // Include the PrometheusMeterRegistry in a possible CompositeMeterRegistry
        return new MicrometerRegistryProviderBuildItem(PrometheusMeterRegistry.class);
    }

    @BuildStep(onlyIf = MicrometerConfig.MicrometerEnabled.class)
    void createRootRegistry(List<MicrometerRegistryProviderBuildItem> providerClasses,
            BuildProducer<GeneratedBeanBuildItem> beanProducer,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        if (providerClasses.isEmpty()) {
            // No MeterRegistries found
            // Create a no-op MeterRegistry so things aren't broken
            additionalBeans.produce(AdditionalBeanBuildItem.builder().setUnremovable()
                    .addBeanClass(NoopMeterRegistryProvider.class).build());
        } else if (providerClasses.size() > 1) {
            // Many MeterRegistries found
            // Create a CompositeMeterRegistry, and add enabled MeterRegistry beans
            GeneratedBeanGizmoAdaptor gizmoAdaptor = new GeneratedBeanGizmoAdaptor(beanProducer);

            String name = this.getClass().getPackage().getName() + ".CompositeMicrometerRegistryProvider";
            try (ClassCreator classCreator = ClassCreator.builder().className(name).classOutput(gizmoAdaptor).build()) {
                classCreator.addAnnotation(ApplicationScoped.class);

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

    @BuildStep(onlyIf = PrometheusConfig.PrometheusEnabled.class)
    @Record(STATIC_INIT)
    void createPrometheusRoute(BuildProducer<RouteBuildItem> routes, HttpRootPathBuildItem httpRoot,
            MicrometerRecorder recorder) {

        // set up prometheus scrape endpoint
        Handler<RoutingContext> handler = new PrometheusScrapeHandler();

        // Exact match for resources matched to the root path
        routes.produce(new RouteBuildItem(pConfig.path, handler));

        // Match paths that begin with the deployment path
        String matchPath = pConfig.path + (pConfig.path.endsWith("/") ? "*" : "/*");
        routes.produce(new RouteBuildItem(matchPath, handler));
    }

    @BuildStep(onlyIf = MicrometerConfig.MicrometerEnabled.class)
    @Record(RUNTIME_INIT)
    void configureRegistry(MicrometerRecorder recorder) {
        recorder.configureRegistry();
    }

    static boolean isInClasspath(DotName classname) {
        try {
            Class.forName(classname.toString());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
