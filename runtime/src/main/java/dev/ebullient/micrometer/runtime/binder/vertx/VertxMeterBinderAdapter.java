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
public class VertxMeterBinderAdapter extends MetricsOptions implements MeterBinder, VertxMetricsFactory, VertxMetrics {
    private static final Logger log = Logger.getLogger(VertxMeterBinderAdapter.class);

    final AtomicReference<MetricsBinder> vertxMetricsBinder = new AtomicReference<>();

    final List<Pattern> ignorePatterns;

    public VertxMeterBinderAdapter(VertxBinderConfig config) {
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
        vertxMetricsBinder.set(new MetricsBinder(registry, this));
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
        MetricsBinder binder = vertxMetricsBinder.get();
        if (binder == null) {
            throw new IllegalStateException("MeterRegistry was not resolved");
        }
        return new VertxHttpServerMetrics(binder);
    }

    static class MetricsBinder {
        final MeterRegistry registry;
        final VertxMeterBinderAdapter meterBinder;

        MetricsBinder(MeterRegistry registry, VertxMeterBinderAdapter meterBinder) {
            this.registry = registry;
            this.meterBinder = meterBinder;
        }

        List<Pattern> getIgnorePatterns() {
            return meterBinder.ignorePatterns;
        }

        public List<Pattern> getMatchPatterns() {
            return Collections.emptyList();
        }
    }
}
