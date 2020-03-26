package dev.ebullient.micrometer.runtime;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;

@Singleton
public class JvmMetricsProvider {

    @Produces
    @Singleton
    @Default
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Produces
    @Singleton
    @Default
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Produces
    @Singleton
    @Default
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Produces
    @Singleton
    @Default
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }
}
