package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.function.Consumer;

import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.VertxOptions;

@Recorder
public class VertxMeterBinderRecorder {
    private static final Logger log = Logger.getLogger(VertxMeterBinderRecorder.class);

    public Consumer<VertxOptions> configureMetricsAdapter() {
        return new Consumer<VertxOptions>() {
            @Override
            public void accept(VertxOptions vertxOptions) {
                log.debug("Adding Micrometer MeterBinder to VertxOptions");
                VertxMeterBinder binder = CDI.current().select(VertxMeterBinder.class).get();
                vertxOptions.setMetricsOptions(binder);
            }
        };
    }
}
