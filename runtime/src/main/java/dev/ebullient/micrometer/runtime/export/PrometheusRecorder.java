package dev.ebullient.micrometer.runtime.export;

import java.util.function.Function;

import dev.ebullient.micrometer.runtime.export.handlers.PrometheusHandler;
import io.prometheus.client.exporter.common.TextFormat;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

@Recorder
public class PrometheusRecorder {
    PrometheusHandler handler;

    public PrometheusHandler getHandler() {
        if (handler == null) {
            handler = new PrometheusHandler();
        }

        return handler;
    }

    public Function<Router, Route> route(String path) {
        return new Function<Router, Route>() {
            @Override
            public Route apply(Router router) {
                Route route = router.route(path)
                        .produces(TextFormat.CONTENT_TYPE_004);
                return route;
            }
        };
    }
}
