package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxMetricsConfig;
import io.vertx.core.net.SocketAddress;

public class MeasureHttpSocket {

    VertxMetricsConfig config;

    public MeasureHttpSocket(VertxMetricsConfig config, SocketAddress remoteAddress, String remoteName) {
        this.config = config;
        System.out.printf("new MeasureHttpSocket %s %s %s\n", config, remoteAddress, remoteName);
    }

    public void disconnected(SocketAddress remoteAddress) {
    }

    public void bytesRead(SocketAddress remoteAddress, long numberOfBytes) {
    }

    public void bytesWritten(SocketAddress remoteAddress, long numberOfBytes) {
    }

}
