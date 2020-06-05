package dev.ebullient.micrometer.runtime;

import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.impl.ConcurrentHashSet;

@Singleton
public class MicrometerLateBinding {
    @Inject
    MeterRegistry meterRegistry;

    private final Set<MeterBinder> lateBinders = new ConcurrentHashSet<>();

    void onApplicationStart(@Observes @Priority(javax.interceptor.Interceptor.Priority.LIBRARY_BEFORE) StartupEvent event) {
        if (lateBinders.isEmpty()) {
            return;
        }

        // Late bind any MeterBinder instances
        for (MeterBinder binder : lateBinders) {
            binder.bindTo(meterRegistry);
        }
    }

    public void addLateBinding(MeterBinder binder) {
        lateBinders.add(binder);
    }
}
