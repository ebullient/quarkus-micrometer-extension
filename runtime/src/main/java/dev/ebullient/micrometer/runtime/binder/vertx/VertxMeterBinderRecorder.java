package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.function.Consumer;

import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;

@Recorder
public class VertxMeterBinderRecorder {

    public Consumer<VertxOptions> configureMetricsAdapter() {
        return new Consumer<VertxOptions>() {
            @Override
            public void accept(VertxOptions vertxOptions) {

            }
        };
    }

    public class VertxMetricsAdapter extends MetricsOptions implements VertxMetricsFactory, VertxMetrics {
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public VertxMetricsFactory getFactory() {
            return this;
        }

        @Override
        public VertxMetrics metrics(VertxOptions vertxOptions) {
            return this;
        }

        @Override
        public MetricsOptions newOptions() {
            return this;
        }

        @Override
        public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
            return new VertxHttpServerMetrics();
        }
    }
}
