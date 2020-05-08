package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;

@Singleton
public class VertxMeterBinder extends MetricsOptions implements MeterBinder, VertxMetricsFactory, VertxMetrics {
    private static final Logger log = Logger.getLogger(VertxMeterBinder.class);

    AtomicReference<MeterRegistry> meterRegistryAtomicReference = new AtomicReference<>();
    final List<Pattern> ignorePatterns;

    public VertxMeterBinder(VertxBinderConfig config) {
        if (config.ignorePatterns.isPresent()) {
            List<String> stringPatterns = config.ignorePatterns.get();
            ignorePatterns = new ArrayList<>(stringPatterns.size());
            for (String s : stringPatterns) {
                ignorePatterns.add(Pattern.compile(s));
            }
        } else {
            ignorePatterns = Collections.emptyList();
        }
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        log.debugf("Bind registry %s to Vertx Metrics", registry);
        meterRegistryAtomicReference.set(registry);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public VertxMetricsFactory getFactory() {
        return this;
    }

    @Override
    public VertxMetrics metrics(VertxOptions vertxOptions) {
        return this;
    }

    @Override
    public MetricsOptions newOptions() {
        return this;
    }

    @Override
    public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
        log.debugf("Create HttpServerMetrics with options %s and address %s", options, localAddress);
        MeterRegistry registry = meterRegistryAtomicReference.get();
        if (registry == null) {
            throw new IllegalStateException("MeterRegistry was not resolved");
        }
        return new VertxHttpServerMetrics(new VertxHttpMetricsConfig(options, registry, ignorePatterns));
    }

    static class VertxHttpMetricsConfig {
        final HttpServerOptions options;
        final MeterRegistry registry;
        final List<Pattern> ignorePatterns;

        VertxHttpMetricsConfig(HttpServerOptions options, MeterRegistry registry, List<Pattern> ignorePatterns) {
            this.options = options;
            this.registry = registry;
            this.ignorePatterns = ignorePatterns;
        }

        MeterRegistry getRegistry() {
            return registry;
        }

        List<Pattern> getIgnorePatterns() {
            return ignorePatterns;
        }
    }
}
