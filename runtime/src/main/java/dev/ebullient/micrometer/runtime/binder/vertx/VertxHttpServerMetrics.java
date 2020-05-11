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

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxMetricsConfig;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

public class VertxHttpServerMetrics
        implements HttpServerMetrics<MeasureRequest, String, MeasureHttpSocket> {

    final VertxMetricsConfig config;

    public VertxHttpServerMetrics(VertxMetricsConfig config) {
        this.config = config;
    }

    /** -> MeasureHttpSocket */
    @Override
    public MeasureHttpSocket connected(SocketAddress remoteAddress, String remoteName) {
        return new MeasureHttpSocket(config)
                .connected(remoteAddress, remoteName);
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
        return new MeasureRequest(config, request).requestBegin();
    }

    /** MeasureHttpSocket -> MeasureRequest */
    @Override
    public MeasureRequest responsePushed(MeasureHttpSocket socketMetric, HttpMethod method, String uri,
            HttpServerResponse response) {
        return new MeasureRequest(config, method, uri).responsePushed(response);
    }

    /** MeasureRequest */
    @Override
    public void requestReset(MeasureRequest requestMetric) {
        requestMetric.requestReset();
    }

    /** MeasureRequest */
    @Override
    public void responseEnd(MeasureRequest requestMetric, HttpServerResponse response) {
        requestMetric.responseEnd(response);
    }

    /** MeasureHttpSocket & MeasureRequest -> MeasureWebSocket */
    @Override
    public String connected(MeasureHttpSocket socketMetric, MeasureRequest requestMetric,
            ServerWebSocket serverWebSocket) {
        config.activeServerWebsocketConnections.increment();
        return requestMetric.requestPath;
    }

    /** MeasureWebSocket */
    @Override
    public void disconnected(String requestPath) {
        config.activeServerWebsocketConnections.decrement();
    }
}
