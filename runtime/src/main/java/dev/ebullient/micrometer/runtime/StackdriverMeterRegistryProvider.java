package dev.ebullient.micrometer.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.Clock;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class StackdriverMeterRegistryProvider {

    StackdriverConfig config;

    @Produces
    @Singleton
    @DefaultBean
    public io.micrometer.stackdriver.StackdriverConfig config() {
        return new MicrometerStackdriverConfig(config);
    }

    @Produces
    @Singleton
    @DefaultBean
    public StackdriverMeterRegistry registry(io.micrometer.stackdriver.StackdriverConfig config, Clock clock) {
        return new StackdriverMeterRegistry(config, clock);
    }

    void setStackdriverConfig(StackdriverConfig config) {
        this.config = config;
    }

    private static class MicrometerStackdriverConfig implements io.micrometer.stackdriver.StackdriverConfig {

        private final String prefix;
        private final Map<String, String> config;

        private MicrometerStackdriverConfig(StackdriverConfig config) {
            this.prefix = "stackdriver";
            this.config = new HashMap<>();
            this.config.put(prefix + ".step", config.step);
            this.config.put(prefix + ".enabled", Boolean.toString(config.enabled));
            this.config.put(prefix + ".projectId", config.projectId.orElse(null));
            this.config.put(prefix + ".resourceType", config.resourceType);
        }

        @Override
        public String get(String key) {
            return config.get(key);
        }

        @Override
        public String prefix() {
            return prefix;
        }

    }

}
