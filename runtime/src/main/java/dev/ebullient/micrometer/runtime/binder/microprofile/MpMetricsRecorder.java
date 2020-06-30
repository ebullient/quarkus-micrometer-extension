package dev.ebullient.micrometer.runtime.binder.microprofile;

import javax.enterprise.inject.spi.BeanManager;

import io.micrometer.core.instrument.*;
import org.eclipse.microprofile.metrics.Metric;
import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InjectableBean;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

@Recorder
public class MpMetricsRecorder {
    private static final Logger log = Logger.getLogger(MpMetricsRecorder.class);

    final Map<Metric, Object> producedMeters = new ConcurrentHashMap<>();

    public void registerMetricFromProducer(RuntimeValue<MeterRegistry> registryValue, String beanId,
            String metricType, String name, String[] tags, String description, String unit) {

        MeterRegistry registry = registryValue.getValue();
        ArcContainer container = Arc.container();
        BeanManager beanManager = container.beanManager();

        InjectableBean<Object> injectableBean = container.bean(beanId);
        Metric reference = (Metric) beanManager.getReference(injectableBean, Metric.class,
                beanManager.createCreationalContext(injectableBean));

        System.out.printf("HERE %s, %s, %s, %s, %s, %s, %s\n", reference, beanId, metricType, name, tags, description,
                unit);

        switch(reference.getClass().getName()) {
            case "org.eclipse.microprofile.metrics.Counter" :
                org.eclipse.microprofile.metrics.Counter counter = (org.eclipse.microprofile.metrics.Counter) reference;
                producedMeters.putIfAbsent(reference,
                    FunctionCounter.builder(name, counter, org.eclipse.microprofile.metrics.Counter::getCount)
                        .description(description)
                        .tags(tags)
                        .register(registry));
                break;

            case "org.eclipse.microprofile.metrics.ConcurrentGauge" :
                org.eclipse.microprofile.metrics.ConcurrentGauge cGauge = (org.eclipse.microprofile.metrics.ConcurrentGauge) reference;
                producedMeters.putIfAbsent(reference,
                    Gauge.builder(name, cGauge::getCount)
                        .description(description)
                        .tags(tags)
                        .baseUnit(unit)
                        .strongReference(true)
                        .register(registry));
                break;

            case "org.eclipse.microprofile.metrics.Gauge":
                org.eclipse.microprofile.metrics.Gauge<Number> gauge = (org.eclipse.microprofile.metrics.Gauge<Number>) reference;
                producedMeters.putIfAbsent(reference,
                    Gauge.builder(name, gauge, g -> (double) g.getValue())
                        .description(description)
                        .tags(tags)
                        .baseUnit(unit)
                        .strongReference(true)
                        .register(registry));
                break;

            case "org.eclipse.microprofile.metrics.Histogram" :
                org.eclipse.microprofile.metrics.Histogram histogram = (org.eclipse.microprofile.metrics.Histogram) reference;
                producedMeters.putIfAbsent(reference,
                    DistributionSummary.builder(name, ))
        }


        if (org.eclipse.microprofile.metrics.Histogram.class.equals(reference.getClass())) {
        } else if (org.eclipse.microprofile.metrics.SimpleTimer.class.equals(reference.getClass())) {
        } else if (org.eclipse.microprofile.metrics.Timer.class.equals(reference.getClass())) {
        } else if (org.eclipse.microprofile.metrics.Metered.class.equals(reference.getClass())) {
        }
    }

    <T> T registerMeter(MeterRegistry registry, Class<T> type,
            String name, String description, String[] tags) {
        if (io.micrometer.core.instrument.Counter.class.equals(type)) {
            log.debugf("Counter for %s", name);
            return type.cast(io.micrometer.core.instrument.Counter.builder(name)
                    .description(description)
                    .tags(tags)
                    .register(registry));
        }
        if (io.micrometer.core.instrument.DistributionSummary.class.equals(type)) {
            log.debugf("DistributionSummary for %s", name);
            return type.cast(DistributionSummary.builder(name)
                    .description(description)
                    .tags(tags)
                    .register(registry));
        }
        if (io.micrometer.core.instrument.Timer.class.equals(type)) {
            log.debugf("Timer for %s", name);
            return type.cast(io.micrometer.core.instrument.Timer.builder(name)
                    .description(description)
                    .tags(tags)
                    .register(registry));
        }
        throw new IllegalArgumentException("Unknown meter type: " + type);
    }
}
