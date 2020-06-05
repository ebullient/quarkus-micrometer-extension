package dev.ebullient.micrometer.runtime.export;

import dev.ebullient.micrometer.runtime.export.handlers.PrometheusHandler;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PrometheusRecorder {
    PrometheusHandler handler;

    public PrometheusHandler getHandler() {
        if (handler == null) {
            handler = new PrometheusHandler();
        }

        return handler;
    }
}
