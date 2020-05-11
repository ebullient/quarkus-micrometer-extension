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

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter.MetricsBinder;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Tags;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

public class VertxHttpServerMetrics
        implements HttpServerMetrics<MeasureRequest, LongTaskTimer.Sample, LongTaskTimer.Sample> {

    final MetricsBinder binder;

    public VertxHttpServerMetrics(MetricsBinder binder) {
        this.binder = binder;
    }

    /** -> MeasureHttpSocket */
    @Override
    public LongTaskTimer.Sample connected(SocketAddress remoteAddress, String remoteName) {
        return LongTaskTimer.builder("http.server.connections")
                .register(binder.registry)
                .start();
    }

    /** MeasureHttpSocket */
    @Override
    public void disconnected(LongTaskTimer.Sample socketMetric, SocketAddress remoteAddress) {
        socketMetric.stop();
    }

    /**
     * Called when bytes have been read
     *
     * @param socketMetric the socket metric, null for UDP
     * @param remoteAddress the remote address which this socket received bytes from
     * @param numberOfBytes the number of bytes read
     */
    @Override
    public void bytesRead(LongTaskTimer.Sample socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
        DistributionSummary.builder("http.server.bytes.read")
                .register(binder.registry)
                .record(numberOfBytes);
    }

    /**
     * Called when bytes have been written
     *
     * @param socketMetric the socket metric, null for UDP
     * @param remoteAddress the remote address which bytes are being written to
     * @param numberOfBytes the number of bytes written
     */
    @Override
    public void bytesWritten(LongTaskTimer.Sample socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
        DistributionSummary.builder("http.server.bytes.written")
                .register(binder.registry)
                .record(numberOfBytes);
    }

    /**
     * Called when exceptions occur for a specific connection.
     *
     * @param socketMetric the socket metric, null for UDP
     * @param remoteAddress the remote address of the connection or null if it's datagram/udp
     * @param t the exception that occurred
     */
    @Override
    public void exceptionOccurred(LongTaskTimer.Sample socketMetric, SocketAddress remoteAddress, Throwable t) {
        binder.registry.counter("http.server.errors", "class", t.getClass().getName()).increment();
    }

    /**
     * Called when an http server response is pushed.
     *
     * @param socketMetric the socket metric
     * @param method the pushed response method
     * @param uri the pushed response uri
     * @param response the http server response
     * @return the request metric
     */
    @Override
    public MeasureRequest responsePushed(LongTaskTimer.Sample socketMetric, HttpMethod method, String uri,
            HttpServerResponse response) {
        return new MeasureRequest(binder, method, uri).responsePushed(response);
    }

    /**
     * Called when an http server request begins. Vert.x will invoke {@link #responseEnd} when the response has ended
     * or {@link #requestReset} if the request/response has failed before.
     *
     * @param socketMetric the socket metric
     * @param request the http server reuqest
     * @return the request metric
     */
    @Override
    public MeasureRequest requestBegin(LongTaskTimer.Sample socketMetric, HttpServerRequest request) {
        return new MeasureRequest(binder, request).requestBegin();
    }

    /**
     * Called when the http server request couldn't complete successfully, for instance the connection
     * was closed before the response was sent.
     *
     * @param requestMetric the request metric
     */
    @Override
    public void requestReset(MeasureRequest requestMetric) {
        requestMetric.requestReset();
    }

    /**
     * Called when an http server response has ended.
     *
     * @param requestMetric the request metric
     * @param response the http server request
     */
    @Override
    public void responseEnd(MeasureRequest requestMetric, HttpServerResponse response) {
        requestMetric.responseEnd(response);
    }

    /** MeasureHttpSocket & MeasureRequest -> MeasureWebSocket */
    @Override
    public LongTaskTimer.Sample connected(LongTaskTimer.Sample socketMetric, MeasureRequest requestMetric,
            ServerWebSocket serverWebSocket) {
        return LongTaskTimer.builder("http.server.websocket.connections")
                .tags(Tags.of(VertxMetricsTags.uri(binder.getMatchPatterns(), requestMetric.requestPath, null)))
                .register(binder.registry)
                .start();
    }

    /** MeasureWebSocket */
    @Override
    public void disconnected(LongTaskTimer.Sample websocketMetric) {
        websocketMetric.stop();
    }
}
