package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter.MetricsBinder;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class MeasureRequest {

    final MetricsBinder binder;
    Timer.Sample sample;
    final HttpMethod method;
    final String requestPath;

    public MeasureRequest(MetricsBinder binder, HttpServerRequest request) {
        this(binder, request.method(), request.uri());
    }

    public MeasureRequest(MetricsBinder binder, HttpMethod method, String uri) {
        this.binder = binder;
        this.method = method;
        this.requestPath = VertxMetricsTags.parseUriPath(this.binder.getIgnorePatterns(), uri);
    }

    public MeasureRequest requestBegin() {
        if (this.requestPath != null) {
            sample = Timer.start(binder.registry);
        }
        return this;
    }

    public MeasureRequest responsePushed(HttpServerResponse response) {
        if (this.requestPath != null) {

            binder.registry.counter("http.server.push", Tags.of(
                    VertxMetricsTags.uri(binder.getMatchPatterns(), requestPath, response),
                    VertxMetricsTags.method(method),
                    VertxMetricsTags.outcome(response),
                    VertxMetricsTags.status(response)))
                    .increment();
        }
        return this;
    }

    public void requestReset() {
        if (sample != null) {
            sample.stop(binder.registry.timer("http.server.requests", Tags.of(
                    VertxMetricsTags.uri(binder.getMatchPatterns(), requestPath, null),
                    VertxMetricsTags.method(method),
                    VertxMetricsTags.OUTCOME_CLIENT_ERROR,
                    VertxMetricsTags.STATUS_RESET)));
        }
    }

    public void responseEnd(HttpServerResponse response) {
        if (sample != null) {
            sample.stop(binder.registry.timer("http.server.requests", Tags.of(
                    VertxMetricsTags.uri(binder.getMatchPatterns(), requestPath, response),
                    VertxMetricsTags.method(method),
                    VertxMetricsTags.outcome(response),
                    VertxMetricsTags.status(response))));
        }
    }
}
