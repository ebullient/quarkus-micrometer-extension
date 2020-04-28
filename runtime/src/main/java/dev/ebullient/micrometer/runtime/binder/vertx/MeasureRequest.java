package dev.ebullient.micrometer.runtime.binder.vertx;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class MeasureRequest {

    public MeasureRequest(MeasureHttpSocket socketMetric) {
    }

    public MeasureRequest(MeasureHttpSocket socketMetric, HttpServerRequest request) {
    }

    public void reset() {
    }

    public void responseBegin(HttpServerResponse response) {
    }

    public void responseEnd(HttpServerResponse response) {
    }

    public MeasureRequest responsePushed(HttpMethod method, String uri, HttpServerResponse response) {
        return this;
    }

}
