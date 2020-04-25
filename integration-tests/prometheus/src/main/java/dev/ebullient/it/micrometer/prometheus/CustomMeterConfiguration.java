package dev.ebullient.it.micrometer.prometheus;

import java.util.Arrays;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import dev.ebullient.micrometer.runtime.MeterFilterConstraint;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusMeterRegistry;

@Singleton
public class CustomMeterConfiguration {

    @Produces
    @Singleton
    @MeterFilterConstraint(applyTo = PrometheusMeterRegistry.class)
    public MeterFilter configurePrometheusRegistries() {
        return MeterFilter.commonTags(Arrays.asList(
                Tag.of("registry", "prometheus")));
    }

    @Produces
    @Singleton
    public MeterFilter configureAllRegistries() {
        return MeterFilter.commonTags(Arrays.asList(
                Tag.of("env", "test")));
    }
}
