package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxHttpMetricsConfig;
import io.vertx.core.net.SocketAddress;

public class MeasureHttpSocket {

    VertxHttpMetricsConfig httpMetricsConfig;

    public MeasureHttpSocket(VertxHttpMetricsConfig httpMetricsConfig, SocketAddress remoteAddress, String remoteName) {
        this.httpMetricsConfig = httpMetricsConfig;
        System.out.printf("new MeasureHttpSocket %s %s %s\n", httpMetricsConfig, remoteAddress, remoteName);
    }

    public void disconnected(SocketAddress remoteAddress) {
    }

    public void bytesRead(SocketAddress remoteAddress, long numberOfBytes) {
    }

    public void bytesWritten(SocketAddress remoteAddress, long numberOfBytes) {
    }

}
