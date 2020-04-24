package dev.ebullient.it.micrometer.prometheus;

import java.util.Arrays;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;

@Singleton
public class CustomMeterConfiguration {

    // @Produces
    // @Singleton
    // @MeterFilterConstraint(type = PrometheusMeterRegistry.class)
    // public MeterFilter configurePrometheusRegistries() {
    //     System.out.println("Configure Prometheus tags");
    //     return MeterFilter.commonTags(Arrays.asList(
    //             Tag.of("registry", "prometheus")));
    // }

    @Produces
    @Singleton
    public MeterFilter configureAllRegistries() {
        System.out.println("Common Registry configuration");
        return MeterFilter.commonTags(Arrays.asList(
                Tag.of("env", "test")));
    }
}
