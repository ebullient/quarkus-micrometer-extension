package dev.ebullient.micrometer.runtime.export;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class PrometheusRecorder {
    private static final Logger log = Logger.getLogger(PrometheusRecorder.class);

    public Handler<RoutingContext> createPrometheusScrapeHandler() {
        final PrometheusMeterRegistry registry;

        Instance<PrometheusMeterRegistry> registries = CDI.current().select(PrometheusMeterRegistry.class,
                Default.Literal.INSTANCE);

        if (registries.isUnsatisfied()) {
            return new Handler<RoutingContext>() {
                @Override
                public void handle(RoutingContext event) {
                    HttpServerResponse response = event.response();
                    response.setStatusCode(500)
                            .setStatusMessage("Unable to resolve Prometheus registry instance");
                }
            };
        } else if (registries.isAmbiguous()) {
            registry = registries.iterator().next();
            log.warnf("Multiple prometheus registries present: %s. Using %s with the built in scrape endpoint", registries,
                    registry);
        } else {
            registry = registries.get();
        }

        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                HttpServerResponse response = event.response();
                response.putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
                        .end(Buffer.buffer(registry.scrape()));
            }
        };
    }

}
