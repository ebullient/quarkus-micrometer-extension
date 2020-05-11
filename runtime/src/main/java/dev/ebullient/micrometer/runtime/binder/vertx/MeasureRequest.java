package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxMetricsConfig;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class MeasureRequest {

    VertxMetricsConfig config;
    Timer.Sample sample;
    HttpMethod method;
    String requestPath;

    public MeasureRequest(VertxMetricsConfig httpMetricsConfig, HttpServerRequest request) {
        this(httpMetricsConfig, request.method(), request.uri());
    }

    public MeasureRequest(VertxMetricsConfig config, HttpMethod method, String uri) {
        this.config = config;
        this.method = method;
        this.requestPath = VertxMetricsTags.parseUriPath(config.getIgnorePatterns(), uri);
    }

    public MeasureRequest requestBegin() {
        this.config.activeHttpServerRequests.increment();
        if (this.requestPath != null) {
            sample = Timer.start(config.registry);
        }
        return this;
    }

    public MeasureRequest responsePushed(HttpServerResponse response) {
        this.config.activeHttpServerRequests.increment();
        return this;
    }

    public void requestReset() {
        config.httpServerRequestReset.tags(Tags.of(
                VertxMetricsTags.uri(config.getMatchPatterns(), requestPath, null),
                VertxMetricsTags.method(method)))
                .register(config.registry)
                .increment();
        this.config.activeHttpServerRequests.decrement();
    }

    public void responseEnd(HttpServerResponse response) {
        if (sample != null) {
            sample.stop(config.registry,
                    config.httpServerRequestsTimer.tags(Tags.of(
                            VertxMetricsTags.uri(config.getMatchPatterns(), requestPath, response),
                            VertxMetricsTags.method(method),
                            VertxMetricsTags.status(response))));
        }
        this.config.activeHttpServerRequests.decrement();
    }
}
