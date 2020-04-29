package dev.ebullient.micrometer.deployment.binder;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import javax.interceptor.Interceptor;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.deployment.MicrometerBuildTimeConfig;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderRecorder;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.vertx.core.deployment.VertxOptionsConsumerBuildItem;

public class VertxBinderProcessor {
    private static final Logger log = Logger.getLogger(VertxBinderProcessor.class);

    static final String METRIC_OPTIONS_CLASS_NAME = "io.vertx.core.VertxOptions";
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
            return METRIC_OPTIONS_CLASS != null && mConfig.checkEnabledWithDefault(config.enabled);
        }
    }

    @BuildStep
    @Record(value = ExecutionTime.RUNTIME_INIT)
    VertxOptionsConsumerBuildItem build(VertxMeterBinderRecorder recorder) {
        return new VertxOptionsConsumerBuildItem(recorder.configureMetricsAdapter(), Interceptor.Priority.LIBRARY_AFTER);
    }
}