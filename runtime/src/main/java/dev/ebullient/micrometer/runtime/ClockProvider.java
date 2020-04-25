package dev.ebullient.micrometer.runtime;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.MeterFilter;
import io.quarkus.arc.DefaultBean;

@Singleton
public class ClockProvider {
    private static final Logger log = Logger.getLogger(ClockProvider.class);

    final Instance<MeterFilter> filters;

    /**
     * Force instantiation of MeterFilters (which shouldn't be cleaned up).
     * Qualified MeterFilters are already specifically referenced by *RegistryProviders.
     * For {@link MicrometerRecorder#configureRegistry(io.quarkus.runtime.ShutdownContext)} to
     * see unqualified MeterFilter instances, something needs to ensure they are instantiated.
     * 
     * @param filters
     */
    ClockProvider(Instance<MeterFilter> filters) {
        log.debugf("ClockProvider initialized. hasFilters=%s", !filters.isUnsatisfied());
        this.filters = filters;
    }

    @Produces
    @Singleton
    @DefaultBean
    public Clock clock() {
        return Clock.SYSTEM;
    }
}
