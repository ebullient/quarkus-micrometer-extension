package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.function.Consumer;

import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class VertxMeterBinderRecorder {
    private static final Logger log = Logger.getLogger(VertxMeterBinderRecorder.class);

    public Consumer<VertxOptions> configureMetricsAdapter() {
        return new Consumer<VertxOptions>() {
            @Override
            public void accept(VertxOptions vertxOptions) {
                log.debug("Adding Micrometer MeterBinder to VertxOptions");
                VertxMeterBinderAdapter binder = CDI.current().select(VertxMeterBinderAdapter.class).get();
                vertxOptions.setMetricsOptions(binder);
            }
        };
    }

    public Handler<RoutingContext> createRouteFilter() {
        return new Handler<RoutingContext>() {

            @Override
            public void handle(final RoutingContext event) {
                final Context context = Vertx.currentContext();
                log.debugf("Handling event %s with context %s", event, context);

                context.put(VertxHttpServerMetrics.ROUTER_CONTEXT, event);
                event.addBodyEndHandler(new Handler<Void>() {

                    @Override
                    public void handle(Void x) {
                        // TODO Auto-generated method stub
                        log.debugf("Handling event %s with context %s at body end", event, context);
                        context.remove(VertxHttpServerMetrics.ROUTER_CONTEXT);
                    }
                });

                event.next();
            }

        };
    }
}
