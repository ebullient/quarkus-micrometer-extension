package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxMetricsConfig;
import io.vertx.core.http.ServerWebSocket;

public class MeasureWebSocket {

    public MeasureWebSocket(VertxMetricsConfig httpMetricsConfig, MeasureRequest requestMetric,
            ServerWebSocket serverWebSocket) {
        System.out.printf("new MeasureWebSocket %s %s %s\n", httpMetricsConfig, requestMetric, serverWebSocket);
    }

    public void disconnected() {
    }
}
