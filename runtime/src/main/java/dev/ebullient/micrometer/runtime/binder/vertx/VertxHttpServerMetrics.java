/*
 * Copyright © 2020 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package dev.ebullient.micrometer.runtime.binder.vertx;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxHttpMetricsConfig;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

public class VertxHttpServerMetrics
        implements HttpServerMetrics<MeasureRequest, MeasureWebSocket, MeasureHttpSocket> {

    final VertxHttpMetricsConfig httpMetricsConfig;

    public VertxHttpServerMetrics(VertxHttpMetricsConfig httpMetricsConfig) {
        this.httpMetricsConfig = httpMetricsConfig;
    }

    /** -> MeasureHttpSocket */
    @Override
    public MeasureHttpSocket connected(SocketAddress remoteAddress, String remoteName) {
        return new MeasureHttpSocket(httpMetricsConfig, remoteAddress, remoteName);
    }

    /** MeasureHttpSocket */
    @Override
    public void disconnected(MeasureHttpSocket socketMetric, SocketAddress remoteAddress) {
        socketMetric.disconnected(remoteAddress);
    }

    /** MeasureHttpSocket */
    @Override
    public void bytesRead(MeasureHttpSocket socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
        socketMetric.bytesRead(remoteAddress, numberOfBytes);
    }

    /** MeasureHttpSocket */
    @Override
    public void bytesWritten(MeasureHttpSocket socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
        socketMetric.bytesWritten(remoteAddress, numberOfBytes);
    }

    /** MeasureHttpSocket -> MeasureRequest */
    @Override
    public MeasureRequest requestBegin(MeasureHttpSocket socketMetric, HttpServerRequest request) {
        return new MeasureRequest(httpMetricsConfig, socketMetric, request);
    }

    /** MeasureRequest */
    @Override
    public void requestReset(MeasureRequest requestMetric) {
        requestMetric.reset();
    }

    /** MeasureRequest */
    @Override
    public void responseBegin(MeasureRequest requestMetric, HttpServerResponse response) {
        requestMetric.responseBegin(response);
    }

    /** MeasureRequest */
    @Override
    public void responseEnd(MeasureRequest requestMetric, HttpServerResponse response) {
        requestMetric.responseEnd(response);
    }

    /** MeasureHttpSocket -> MeasureRequest */
    @Override
    public MeasureRequest responsePushed(MeasureHttpSocket socketMetric, HttpMethod method, String uri,
            HttpServerResponse response) {
        return new MeasureRequest(httpMetricsConfig, socketMetric).responsePushed(method, uri, response);
    }

    /** MeasureHttpSocket & MeasureRequest -> MeasureWebSocket */
    @Override
    public MeasureWebSocket connected(MeasureHttpSocket socketMetric, MeasureRequest requestMetric,
            ServerWebSocket serverWebSocket) {
        return new MeasureWebSocket(httpMetricsConfig, requestMetric, serverWebSocket);
    }

    /** MeasureWebSocket */
    @Override
    public void disconnected(MeasureWebSocket serverWebSocketMetric) {
        serverWebSocketMetric.disconnected();
    }
}
