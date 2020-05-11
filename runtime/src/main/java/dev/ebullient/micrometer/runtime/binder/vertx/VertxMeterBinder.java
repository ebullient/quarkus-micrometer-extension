package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
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

    AtomicReference<VertxMetricsConfig> vertxMetricsConfig = new AtomicReference<>();

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
        vertxMetricsConfig.set(new VertxMetricsConfig(registry, this));
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
        VertxMetricsConfig metricsConfig = vertxMetricsConfig.get();
        if (metricsConfig == null) {
            throw new IllegalStateException("MeterRegistry was not resolved");
        }
        return new VertxHttpServerMetrics(metricsConfig);
    }

    static class VertxMetricsConfig {
        final MeterRegistry registry;
        final VertxMeterBinder meterBinder;

        final LongAdder activeHttpServerRequests;
        final LongAdder activeServerWebsocketConnections;
        final LongAdder activeHttpConnections;
        final Map<String, Counter> httpErrorCount;

        final Counter.Builder httpServerRequestReset;
        final Timer.Builder httpServerRequestsTimer;
        final DistributionSummary.Builder httpBytesRead;
        final DistributionSummary.Builder httpBytesWritten;

        VertxMetricsConfig(MeterRegistry registry, VertxMeterBinder meterBinder) {
            this.registry = registry;
            this.meterBinder = meterBinder;
            this.httpServerRequestReset = Counter.builder("http.server.request.reset");
            this.httpServerRequestsTimer = Timer.builder("http.server.requests");
            this.httpBytesRead = DistributionSummary.builder("http.server.bytes.read");
            this.httpBytesWritten = DistributionSummary.builder("http.server.bytes.written");

            this.activeHttpServerRequests = registry.gauge("http.server.requests.active", new LongAdder());
            this.activeServerWebsocketConnections = registry.gauge("http.server.websockets.active", new LongAdder());
            this.activeHttpConnections = registry.gauge("http.server.connections", new LongAdder());
            this.httpErrorCount = new HashMap<String, Counter>();
        }

        void incrementErrorCount(String exceptionName) {
            httpErrorCount.computeIfAbsent(exceptionName, k -> Counter.builder(k).tag("class", k).register(registry))
                    .increment();
        }

        List<Pattern> getIgnorePatterns() {
            return meterBinder.ignorePatterns;
        }

        public List<Pattern> getMatchPatterns() {
            return Collections.emptyList();
        }
    }
}
