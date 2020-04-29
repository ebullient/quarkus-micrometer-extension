package dev.ebullient.micrometer.runtime.binder.vertx;

import javax.inject.Singleton;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@Singleton
public class VertxMeterBinder implements MeterBinder {

    @Override
    public void bindTo(MeterRegistry registry) {
        // TODO Auto-generated method stub
    }
}
