package dev.ebullient.micrometer.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.stackdriver.StackdriverConfig;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class StackdriverMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(StackdriverMeterRegistryProvider.class);

    static final String PREFIX = "quarkus.micrometer.export.stackdriver.";
    static final int TRIM_POS = "quarkus.micrometer.export.".length();
    static final String PUBLISH = "stackdriver.publish";

    StackdriverMeterRegistryProvider() {
        log.debug("StackdriverMeterRegistryProvider initialized");
    }

    @Produces
    @Singleton
    @DefaultBean
    public StackdriverConfig configure(Config config, Clock clock) {
        final Map<String, String> properties = new HashMap<>();

        // Rename and store stackdriver properties
        for (String name : config.getPropertyNames()) {
            if (name.startsWith(PREFIX)) {
                String key = convertKey(name);
                String value = config.getValue(name, String.class);
                properties.put(key, value);
            }
        }

        // Special check: if publish is set, override the value of enabled
        // Specifically, The stackdriver registry must be enabled for this
        // Provider to even be present. If this instance (at runtime) wants
        // to prevent
        if (properties.containsKey(PUBLISH)) {
            properties.put("stackdriver.enabled", properties.get(PUBLISH));
        }

        return new StackdriverConfig() {
            @Override
            public String get(String key) {
                return properties.get(key);
            }
        };
    }

    @Produces
    @Singleton
    @DefaultBean
    public StackdriverMeterRegistry registry(StackdriverConfig config, Clock clock) {
        return new StackdriverMeterRegistry(config, clock);
    }

    private String convertKey(String name) {
        String key = name.substring(TRIM_POS);
        key = MicrometerRecorder.camelHumpify(key);
        return key;
    }
}
