package dev.ebullient.micrometer.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import dev.ebullient.micrometer.runtime.ClockProvider;
import dev.ebullient.micrometer.runtime.CompositeMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.JvmMetricsProvider;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.NoopMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.PrometheusMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.PrometheusScrapeHandler;
import dev.ebullient.micrometer.runtime.SystemMetricsProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class MicrometerProcessor {
    private static final String FEATURE = "micrometer";

    @ConfigRoot(name = "micrometer")
    static final class MicrometerConfig {
        /**
         * Default path for the metrics handler
         */
        @ConfigItem(defaultValue = "/micrometer")
        String path;
    }

    @ConfigRoot(name = "micrometer-prometheus")
    static final class PrometheusConfig {
        /**
         * Default path for the prometheus endpoint
         */
        @ConfigItem(defaultValue = "/prometheus")
        String path;
    }

    MicrometerConfig mConfig;
    PrometheusConfig pConfig;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    // @BuildStep
    // public CapabilityBuildItem capability() {
    // return new CapabilityBuildItem(Capabilities.METRICS);
    // }

    @BuildStep
    void addInfinispanDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.micrometer", "micrometer-core"));
        indexDependency.produce(new IndexDependencyBuildItem("io.micrometer", "micrometer-registry-prometheus"));
    }

    @BuildStep
    void registerAdditionalBeans(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<UnremovableBeanBuildItem> unremovableBean) {

        System.out.println(index.getIndex().getAllKnownSubclasses(MicrometerDotNames.METER_REGISTRY));
        System.out.println(index.getIndex().getAllKnownImplementors(MicrometerDotNames.METER_BINDER));

        // CDI Provider that will create/inject default providers
        additionalBeans.produce(new AdditionalBeanBuildItem(ClockProvider.class));

        // Create and keep JVM/System MeterBinders
        additionalBeans.produce(new AdditionalBeanBuildItem(JvmMetricsProvider.class));
        additionalBeans.produce(new AdditionalBeanBuildItem(SystemMetricsProvider.class));
        unremovableBean.produce(new UnremovableBeanBuildItem(
                new UnremovableBeanBuildItem.BeanClassNamesExclusion(new HashSet<>(Arrays.asList(
                        JvmMetricsProvider.class.getName(),
                        SystemMetricsProvider.class.getName())))));

        // Preserve MeterFilter beans
        Collection<ClassInfo> filters = index.getIndex().getAllKnownImplementors(MicrometerDotNames.METER_FILTER);
        filters.stream().forEach(System.out::println);
        // unremovableBean.produce(new UnremovableBeanBuildItem(
        //         new UnremovableBeanBuildItem.BeanClassNamesExclusion(new HashSet<String>(
        //                 filters.stream()
        //                         .map(ci -> ci.name().toString())
        //                         .collect(Collectors.toSet())))));

        int numRegistries = 0;

        if (isInClasspath(MicrometerDotNames.PROMETHEUS_REGISTRY)) {
            numRegistries++;
            // CDI Provider that will create/inject Prometheus MeterRegistry
            additionalBeans.produce(new AdditionalBeanBuildItem(PrometheusMeterRegistryProvider.class));
        }

        if (numRegistries == 0) {
            // CDI Provider that will create/inject default No-Op Micrometer instance
            additionalBeans.produce(new AdditionalBeanBuildItem(NoopMeterRegistryProvider.class));
        } else if (numRegistries == 1) {
            // Do nothing. The single registry above will be the registry. The end. ;)
            // TEST: additionalBeans.produce(new AdditionalBeanBuildItem(CompositeMeterRegistryProvider.class));
        } else if (numRegistries > 1) {
            additionalBeans.produce(new AdditionalBeanBuildItem(CompositeMeterRegistryProvider.class));
        }
    }

    @BuildStep
    @Record(STATIC_INIT)
    void createRoute(BuildProducer<RouteBuildItem> routes,
            HttpRootPathBuildItem httpRoot,
            MicrometerRecorder recorder) {

        // set up prometheus scrape endpoint
        if (isInClasspath(MicrometerDotNames.PROMETHEUS_REGISTRY)) {
            Handler<RoutingContext> handler = new PrometheusScrapeHandler();

            // Exact match for resources matched to the root path
            routes.produce(new RouteBuildItem(pConfig.path, handler));

            // Match paths that begin with the deployment path
            String matchPath = pConfig.path + (pConfig.path.endsWith("/") ? "*" : "/*");
            routes.produce(new RouteBuildItem(matchPath, handler));
        }
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    void configureRegistry(MicrometerRecorder recorder,
            ShutdownContextBuildItem shutdown) {
        // configure registry (with Binders)
        recorder.configureRegistry();
    }

    private static boolean isInClasspath(DotName classname) {
        try {
            Class.forName(classname.toString());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
