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

import javax.annotation.Nullable;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter.MetricsBinder;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.ext.web.RoutingContext;

public class VertxHttpServerMetrics implements HttpServerMetrics<Context, LongTaskTimer.Sample, Context> {
    static final Logger log = Logger.getLogger(VertxHttpServerMetrics.class);

    static final String METER_HTTP_SOCKET_METRIC = "METER_HTTP_SOCKET_METRIC";
    static final String METER_HTTP_REQUEST_PATH = "METER_HTTP_REQUEST_PATH";
    static final String METER_HTTP_REQUEST_SAMPLE = "METER_HTTP_REQUEST_SAMPLE";
    static final String METER_ROUTING_CONTEXT = "METER_ROUTING_CONTEXT";

    final MetricsBinder binder;

    public VertxHttpServerMetrics(MetricsBinder binder) {
        this.binder = binder;
    }

    private void cleanUp(Context source) {
        source.remove(METER_HTTP_REQUEST_PATH);
        source.remove(METER_HTTP_REQUEST_SAMPLE);
        source.remove(METER_ROUTING_CONTEXT);
        source.remove(METER_HTTP_SOCKET_METRIC);
    }

    /**
     * Called when a client has connected, which is applicable for TCP connections.
     * <p/>
     *
     * The remote name of the client is a best effort to provide the name of the
     * remote host, i.e if the name is specified at creation time, this name will be
     * used otherwise it will be the remote address.
     *
     * @param remoteAddress the remote address of the client
     * @param remoteName the remote name of the client
     * @return the socket metric
     */
    @Override
    public Context connected(SocketAddress remoteAddress, String remoteName) {
        Context context = getCurrentContext("connected");
        context.put(METER_HTTP_SOCKET_METRIC,
                LongTaskTimer.builder("http.server.connections").register(binder.registry).start());
        return context;
    }

    /**
     * Called when a client has disconnected, which is applicable for TCP
     * connections.
     *
     * @param socketMetric the socket metric
     * @param remoteAddress the remote address of the client
     */
    @Override
    public void disconnected(Context socketMetric, SocketAddress remoteAddress) {
        log.debugf("Disconnected %s", socketMetric);
        LongTaskTimer.Sample sample = (LongTaskTimer.Sample) socketMetric.get(METER_HTTP_SOCKET_METRIC);
        sample.stop();
        cleanUp(socketMetric);
    }

    /**
     * Called when bytes have been read
     *
     * @param socketMetric the socket metric, null for UDP
     * @param remoteAddress the remote address which this socket received bytes from
     * @param numberOfBytes the number of bytes read
     */
    @Override
    public void bytesRead(Context socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
        DistributionSummary.builder("http.server.bytes.read").register(binder.registry).record(numberOfBytes);
    }

    /**
     * Called when bytes have been written
     *
     * @param socketMetric the socket metric, null for UDP
     * @param remoteAddress the remote address which bytes are being written to
     * @param numberOfBytes the number of bytes written
     */
    @Override
    public void bytesWritten(Context socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
        DistributionSummary.builder("http.server.bytes.written").register(binder.registry).record(numberOfBytes);
    }

    /**
     * Called when exceptions occur for a specific connection.
     *
     * @param socketMetric the socket metric, null for UDP
     * @param remoteAddress the remote address of the connection or null if it's
     *        datagram/udp
     * @param t the exception that occurred
     */
    @Override
    public void exceptionOccurred(Context socketMetric, SocketAddress remoteAddress, Throwable t) {
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
    public Context responsePushed(Context socketMetric, HttpMethod method, String uri, HttpServerResponse response) {
        String path = VertxMetricsTags.parseUriPath(this.binder.getMatchPatterns(), this.binder.getIgnorePatterns(),
                uri);
        if (path != null) {
            binder.registry.counter("http.server.push",
                    Tags.of(VertxMetricsTags.uri(path, response), VertxMetricsTags.method(method),
                            VertxMetricsTags.outcome(response), VertxMetricsTags.status(response)))
                    .increment();
        }
        return socketMetric;
    }

    /**
     * Called when an http server request begins. Vert.x will invoke
     * {@link #responseEnd} when the response has ended or {@link #requestReset} if
     * the request/response has failed before.
     *
     * @param socketMetric the socket metric
     * @param request the http server reuqest
     * @return the request metric
     */
    @Override
    public Context requestBegin(Context socketMetric, HttpServerRequest request) {
        String path = VertxMetricsTags.parseUriPath(binder.getMatchPatterns(), binder.getIgnorePatterns(),
                request.uri());
        if (path != null) {
            // Pre-add the request method tag to the sample
            socketMetric.put(METER_HTTP_REQUEST_SAMPLE,
                    Timer.start(binder.registry).tags(Tags.of(VertxMetricsTags.method(request.method()))));

            // remember the path to monitor for use later (maybe a 404 or redirect..)
            socketMetric.put(METER_HTTP_REQUEST_PATH, path);
        }
        return socketMetric;
    }

    /**
     * Called when the http server request couldn't complete successfully, for
     * instance the connection was closed before the response was sent.
     *
     * @param requestMetric the request metric
     */
    @Override
    public void requestReset(Context requestMetric) {
        Timer.Sample sample = requestMetric.get(METER_HTTP_REQUEST_SAMPLE);
        if (sample != null) {
            String requestPath = getRequestPath("requestReset", requestMetric);
            sample.stop(binder.registry,
                    Timer.builder("http.server.requests").tags(Tags.of(VertxMetricsTags.uri(requestPath, null),
                            VertxMetricsTags.OUTCOME_CLIENT_ERROR, VertxMetricsTags.STATUS_RESET)));
        }
    }

    /**
     * Called when an http server response has ended.
     *
     * @param requestMetric the request metric
     * @param response the http server request
     */
    @Override
    public void responseEnd(Context requestMetric, HttpServerResponse response) {
        Timer.Sample sample = requestMetric.get(METER_HTTP_REQUEST_SAMPLE);
        if (sample != null) {
            String requestPath = getRequestPath("responseEnd", requestMetric);

            sample.stop(binder.registry, Timer.builder("http.server.requests")
                    .tags(Tags.of(
                            VertxMetricsTags.uri(requestPath, response),
                            VertxMetricsTags.outcome(response),
                            VertxMetricsTags.status(response))));
        }
    }

    /**
     * Called when a server web socket connects.
     *
     * @param socketMetric the socket metric
     * @param requestMetric the request metric
     * @param serverWebSocket the server web socket
     * @return the server web socket metric
     */
    @Override
    public LongTaskTimer.Sample connected(Context socketMetric, Context requestMetric,
            ServerWebSocket serverWebSocket) {
        String path = getRequestPath("connected", socketMetric);
        if (path != null) {
            return LongTaskTimer.builder("http.server.websocket.connections")
                    .tags(Tags.of(VertxMetricsTags.uri(path, null))).register(binder.registry).start();
        }
        return null;
    }

    /**
     * Called when the server web socket has disconnected.
     *
     * @param websocketMetric the server web socket metric
     */
    @Override
    public void disconnected(LongTaskTimer.Sample websocketMetric) {
        if (websocketMetric != null) {
            websocketMetric.stop();
        }
    }

    private String getRequestPath(String caller, Context source) {
        RoutingContext rctx = getRoutingContext(caller, source);
        if (rctx != null) {
            String path = rctx.get(METER_HTTP_REQUEST_PATH);
            if (path != null) {
                log.debugf("Using path from routing context %s", path);
                return path;
            }
        }
        return source.get(METER_HTTP_REQUEST_PATH);
    }

    private RoutingContext getRoutingContext(String caller, Context source) {
        @Nullable
        RoutingContext routingContext = source.get(METER_ROUTING_CONTEXT);
        return routingContext;
    }

    private Context getCurrentContext(String caller) {
        @Nullable
        Context context = Vertx.currentContext();
        return context;
    }
}
