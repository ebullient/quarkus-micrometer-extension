package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter.MetricsBinder;
import io.vertx.core.net.SocketAddress;

public class MeasureHttpSocket {

    MetricsBinder binder;

    public MeasureHttpSocket(MetricsBinder binder) {
        this.binder = binder;
    }

    public MeasureHttpSocket connected(SocketAddress remoteAddress, String remoteName) {
        this.binder.activeHttpConnections.increment();
        return this;
    }

    public MeasureHttpSocket disconnected(SocketAddress remoteAddress) {
        this.binder.activeHttpConnections.decrement();
        return this;
    }

    public MeasureHttpSocket bytesRead(SocketAddress remoteAddress, long numberOfBytes) {
        this.binder.httpBytesRead
                // tags
                .register(binder.registry)
                .record(numberOfBytes);
        return this;
    }

    public MeasureHttpSocket bytesWritten(SocketAddress remoteAddress, long numberOfBytes) {
        this.binder.httpBytesWritten
                // tags
                .register(binder.registry)
                .record(numberOfBytes);
        return this;
    }

    public MeasureHttpSocket exceptionOccurred(String remote, SocketAddress remoteAddress, Throwable t) {
        this.binder.incrementErrorCount(t.getClass().getName());
        return this;
    }
}
