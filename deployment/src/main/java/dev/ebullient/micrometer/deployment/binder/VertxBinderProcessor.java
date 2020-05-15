package dev.ebullient.micrometer.deployment.binder;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import javax.interceptor.Interceptor;

import dev.ebullient.micrometer.deployment.MicrometerBuildTimeConfig;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.vertx.core.deployment.VertxOptionsConsumerBuildItem;
import io.quarkus.vertx.http.deployment.FilterBuildItem;

public class VertxBinderProcessor {

    static final String METRIC_OPTIONS_CLASS_NAME = "io.vertx.core.metrics.MetricsOptions";
    static final Class<?> METRIC_OPTIONS_CLASS = MicrometerRecorder.getClassForName(METRIC_OPTIONS_CLASS_NAME);

    @ConfigRoot(name = "micrometer.binder.vertx", phase = ConfigPhase.BUILD_TIME)
    static class VertxBuildTimeConfig {
        /**
         * If the Vert.x micrometer meter binder is enabled.
         */
        @ConfigItem
        Optional<Boolean> enabled;

        @Override
        public String toString() {
            return this.getClass().getSimpleName()
                    + "{enabled=" + enabled
                    + '}';
        }
    }

    static class VertxBinderEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;
        VertxBuildTimeConfig config;

        public boolean getAsBoolean() {
            return METRIC_OPTIONS_CLASS != null && mConfig.checkBinderEnabledWithDefault(config.enabled);
        }
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    void createVertxAdapters(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Add Vertx meter adapters
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(VertxMeterBinderAdapter.class)
                .addBeanClass("dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderContainerFilter")
                .setUnremovable().build());
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    void createVertxFilters(BuildProducer<ResteasyJaxrsProviderBuildItem> jaxRsProviders) {

        jaxRsProviders.produce(
                new ResteasyJaxrsProviderBuildItem(
                        "dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderContainerFilter"));
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    @Record(value = ExecutionTime.RUNTIME_INIT)
    VertxOptionsConsumerBuildItem build(VertxMeterBinderRecorder recorder,
            BuildProducer<FilterBuildItem> filterBuildItemBuildProducer) {

        filterBuildItemBuildProducer.produce(new FilterBuildItem(recorder.createRouteFilter(), 10));

        return new VertxOptionsConsumerBuildItem(recorder.configureMetricsAdapter(), Interceptor.Priority.LIBRARY_AFTER);
    }
}
