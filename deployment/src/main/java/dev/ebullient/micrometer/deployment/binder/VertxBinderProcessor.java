package dev.ebullient.micrometer.deployment.binder;

import java.util.function.BooleanSupplier;

import javax.interceptor.Interceptor;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderRecorder;
import dev.ebullient.micrometer.runtime.config.MicrometerConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.vertx.core.deployment.VertxOptionsConsumerBuildItem;
import io.quarkus.vertx.http.deployment.FilterBuildItem;

public class VertxBinderProcessor {

    static final String METRIC_OPTIONS_CLASS_NAME = "io.vertx.core.metrics.MetricsOptions";
    static final Class<?> METRIC_OPTIONS_CLASS = MicrometerRecorder.getClassForName(METRIC_OPTIONS_CLASS_NAME);

    static class VertxBinderEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            return METRIC_OPTIONS_CLASS != null && mConfig.checkBinderEnabledWithDefault(mConfig.binder.vertx);
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
