package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxHttpMetricsConfig;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class MeasureRequest {

    final VertxHttpMetricsConfig httpMetricsConfig;
    final Timer.Sample sample;
    final HttpMethod method;
    final String requestPath;

    public MeasureRequest(VertxHttpMetricsConfig httpMetricsConfig, MeasureHttpSocket socketMetric) {
        this(httpMetricsConfig, socketMetric, null);
    }

    public MeasureRequest(VertxHttpMetricsConfig httpMetricsConfig, MeasureHttpSocket socketMetric, HttpServerRequest request) {
        this.requestPath = request == null ? null : HttpMetricsTags.parseUriPath(httpMetricsConfig, request.uri());
        this.httpMetricsConfig = httpMetricsConfig;
        System.out.printf("new MeasureRequest %s %s %s\n", httpMetricsConfig, socketMetric, requestPath);

        if (this.requestPath != null) {
            sample = Timer.start(httpMetricsConfig.getRegistry());
            method = request.method();
        } else {
            sample = null;
            method = null;
        }
    }

    public void reset() {
    }

    public void responseBegin(HttpServerResponse response) {
    }

    public void responseEnd(HttpServerResponse response) {
        if (requestPath != null) {
            System.out.printf("ReponseEnd %s\n", this.requestPath);
            Timer.Builder builder = Timer.builder("http.server.requests")
                    .tags(Tags.of(HttpMetricsTags.method(method),
                            HttpMetricsTags.uri(httpMetricsConfig, requestPath, response),
                            HttpMetricsTags.status(response)));

            sample.stop(httpMetricsConfig.getRegistry(), builder);
        }
    }

    public MeasureRequest responsePushed(HttpMethod method, String uri, HttpServerResponse response) {
        return this;
    }

}
