package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxMetricsConfig;
import io.vertx.core.net.SocketAddress;

public class MeasureHttpSocket {

    VertxMetricsConfig config;

    public MeasureHttpSocket(VertxMetricsConfig config) {
        this.config = config;
    }

    public MeasureHttpSocket connected(SocketAddress remoteAddress, String remoteName) {
        this.config.activeHttpConnections.increment();
        return this;
    }

    public MeasureHttpSocket disconnected(SocketAddress remoteAddress) {
        this.config.activeHttpConnections.decrement();
        return this;
    }

    public MeasureHttpSocket bytesRead(SocketAddress remoteAddress, long numberOfBytes) {
        this.config.httpBytesRead
                // tags
                .register(config.registry)
                .record(numberOfBytes);
        return this;
    }

    public MeasureHttpSocket bytesWritten(SocketAddress remoteAddress, long numberOfBytes) {
        this.config.httpBytesWritten
                // tags
                .register(config.registry)
                .record(numberOfBytes);
        return this;
    }

    public MeasureHttpSocket exceptionOccurred(String remote, SocketAddress remoteAddress, Throwable t) {
        this.config.incrementErrorCount(t.getClass().getName());
        return this;
    }
}
