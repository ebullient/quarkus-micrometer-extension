package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter.MetricsBinder;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class MeasureRequest {

    MetricsBinder binder;
    Timer.Sample sample;
    HttpMethod method;
    String requestPath;

    public MeasureRequest(MetricsBinder binder, HttpServerRequest request) {
        this(binder, request.method(), request.uri());
    }

    public MeasureRequest(MetricsBinder binder, HttpMethod method, String uri) {
        this.binder = binder;
        this.method = method;
        this.requestPath = VertxMetricsTags.parseUriPath(this.binder.getIgnorePatterns(), uri);
    }

    public MeasureRequest requestBegin() {
        this.binder.activeHttpServerRequests.increment();
        if (this.requestPath != null) {
            sample = Timer.start(binder.registry);
        }
        return this;
    }

    public MeasureRequest responsePushed(HttpServerResponse response) {
        this.binder.activeHttpServerRequests.increment();
        return this;
    }

    public void requestReset() {
        binder.httpServerRequestReset.tags(Tags.of(
                VertxMetricsTags.uri(binder.getMatchPatterns(), requestPath, null),
                VertxMetricsTags.method(method)))
                .register(binder.registry)
                .increment();
        this.binder.activeHttpServerRequests.decrement();
    }

    public void responseEnd(HttpServerResponse response) {
        if (sample != null) {
            sample.stop(binder.registry,
                    binder.httpServerRequestsTimer.tags(Tags.of(
                            VertxMetricsTags.uri(binder.getMatchPatterns(), requestPath, response),
                            VertxMetricsTags.method(method),
                            VertxMetricsTags.status(response))));
        }
        this.binder.activeHttpServerRequests.decrement();
    }
}
