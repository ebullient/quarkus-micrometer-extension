package dev.ebullient.micrometer.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.Clock;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
public class ClockProvider {

    @Produces
    @Singleton
    @DefaultBean
    public Clock clock() {
        return Clock.SYSTEM;
    }
}
