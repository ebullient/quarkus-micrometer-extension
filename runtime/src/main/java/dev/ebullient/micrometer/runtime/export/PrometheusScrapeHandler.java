package dev.ebullient.micrometer.runtime.export;

import javax.enterprise.inject.spi.CDI;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class PrometheusScrapeHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext event) {
        PrometheusMeterRegistry registry = CDI.current().select(PrometheusMeterRegistry.class).get();

        HttpServerResponse response = event.response();
        response.putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
                .end(Buffer.buffer(registry.scrape()));
    }
}
