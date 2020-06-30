package dev.ebullient.micrometer.runtime.binder.microprofile.metric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;

import org.eclipse.microprofile.metrics.ConcurrentGauge;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.jboss.logging.Logger;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Create default producer methods for {literal @}Inject {literal @}Metric
 * annotations requiring {@code Meter}, {@code Timer}, {@code Counter},
 * and {@code Histogram}.
 *
 * Due to build-time processing, {literal @}Metric annotations always have
 * a name value that has been resolved according to MP Metrics naming conventions.
 */
@SuppressWarnings("unused")
@Singleton
public class InjectedMetricProducer {
    private static final Logger log = Logger.getLogger(InjectedMetricProducer.class);

    static final Class<?> micrometerCounter = io.micrometer.core.instrument.Counter.class;
    static final Class<?> micrometerSummary = io.micrometer.core.instrument.DistributionSummary.class;
    static final Class<?> micrometerTimer = io.micrometer.core.instrument.Timer.class;

    // Micrometer meter registry
    final MeterRegistry registry;

    // Annotation-derived meter instances
    final Map<Metric, Object> annotationMeters = new ConcurrentHashMap<>();

    InjectedMetricProducer(MeterRegistry registry) {
        this.registry = registry;
    }

    @Produces
    Counter getCounter(InjectionPoint ip) {
        Metric metric = ip.getAnnotated().getAnnotation(Metric.class);
        log.debugf("Counter for %s", metric);
        Object o = annotationMeters.computeIfAbsent(metric,
                k -> new CounterAdapter(registerMeter(io.micrometer.core.instrument.Counter.class, metric)));
        return checkCast(Counter.class, metric, o);
    }

    /**
     * For a programmatic concurrent gauge, create a gauge around
     * a simple implementation that uses a {@code LongAdder}.
     * The metrics gathered this way will not be as rich as with the
     * {@code LongTimerTask}-based metrics used with the
     * {literal @}ConcurrentGauge annotation, but is the best the API
     * semantics allow (decrement/increment).
     */
    @Produces
    ConcurrentGauge getConcurrentGauge(InjectionPoint ip) {
        Metric metric = ip.getAnnotated().getAnnotation(Metric.class);
        log.debugf("ConcurrentGauge for %s", metric);
        Object o = annotationMeters.computeIfAbsent(metric, k -> {
            ConcurrentGaugeImpl impl = new ConcurrentGaugeImpl();
            io.micrometer.core.instrument.Gauge.builder(metric.name(), impl::getCount)
                    .description(metric.description())
                    .tags(metric.tags())
                    .baseUnit(metric.unit())
                    .strongReference(true)
                    .register(registry);
            return impl;
        });
        return checkCast(ConcurrentGauge.class, metric, o);
    }

    @Produces
    Histogram getHistogram(InjectionPoint ip) {
        Metric metric = ip.getAnnotated().getAnnotation(Metric.class);
        Object o = annotationMeters.computeIfAbsent(metric,
                k -> new HistogramAdapter(registerMeter(io.micrometer.core.instrument.DistributionSummary.class, metric)));
        return checkCast(Histogram.class, metric, o);
    }

    @Produces
    Meter getMeter(InjectionPoint ip) {
        Metric metric = ip.getAnnotated().getAnnotation(Metric.class);
        Object o = annotationMeters.computeIfAbsent(metric,
                k -> new MeterAdapter(registerMeter(io.micrometer.core.instrument.Counter.class, metric)));
        return checkCast(Meter.class, metric, o);
    }

    @Produces
    SimpleTimer getSimpleTimer(InjectionPoint ip) {
        Metric metric = ip.getAnnotated().getAnnotation(Metric.class);
        Object o = annotationMeters.computeIfAbsent(metric,
                k -> new TimerAdapter(registerMeter(io.micrometer.core.instrument.Timer.class, metric), registry));
        return checkCast(SimpleTimer.class, metric, o);
    }

    @Produces
    Timer getTimer(InjectionPoint ip) {
        Metric metric = ip.getAnnotated().getAnnotation(Metric.class);
        Object o = annotationMeters.computeIfAbsent(metric,
                k -> new TimerAdapter(registerMeter(io.micrometer.core.instrument.Timer.class, metric), registry));
        return checkCast(Timer.class, metric, o);
    }

    <T> T registerMeter(Class<T> type, Metric metric) {
        if (io.micrometer.core.instrument.Counter.class.equals(type)) {
            log.debugf("Counter for %s", metric);
            return type.cast(io.micrometer.core.instrument.Counter.builder(metric.name())
                    .description(metric.description())
                    .tags(metric.tags())
                    .register(registry));
        }
        if (io.micrometer.core.instrument.DistributionSummary.class.equals(type)) {
            log.debugf("DistributionSummary for %s", metric);
            return type.cast(DistributionSummary.builder(metric.name())
                    .description(metric.description())
                    .tags(metric.tags())
                    .register(registry));
        }
        if (io.micrometer.core.instrument.Timer.class.equals(type)) {
            log.debugf("Timer for %s", metric);
            return type.cast(io.micrometer.core.instrument.Timer.builder(metric.name())
                    .description(metric.description())
                    .tags(metric.tags())
                    .register(registry));
        }
        throw new IllegalArgumentException("Unknown meter type: " + type);
    }

    private <T> T checkCast(Class<T> type, Metric m, Object o) {
        try {
            return type.cast(o);
        } catch (ClassCastException cce) {
            log.errorf("Unable to find or create metric for %s. " +
                    "Metric already exists as another type %s",
                    m, o.getClass());
            throw new IllegalArgumentException(cce);
        }
    }
}
