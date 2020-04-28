package dev.ebullient.micrometer.runtime.binder.vertx;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;

public class VertxMetricsAdapter implements VertxMetrics, MeterBinder {

    @Override
    public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
        return new VertxHttpServerMetrics();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        // TODO Auto-generated method stub

    }
}
