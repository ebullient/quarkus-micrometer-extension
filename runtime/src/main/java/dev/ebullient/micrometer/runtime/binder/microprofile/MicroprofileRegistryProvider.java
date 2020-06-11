package dev.ebullient.micrometer.runtime.binder.microprofile;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.metrics.*;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import io.micrometer.core.instrument.MeterRegistry;

@Singleton
public class MicroprofileRegistryProvider {

    private static final Map<MetricRegistry.Type, MetricRegistry> registries = new ConcurrentHashMap<>(3);

    @Inject
    MeterRegistry meterRegistry;

    @Produces
    @Default
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    @ApplicationScoped
    public MetricRegistry getApplicationRegistry() {
        return get(MetricRegistry.Type.APPLICATION, meterRegistry);
    }

    @Produces
    @RegistryType(type = MetricRegistry.Type.BASE)
    @ApplicationScoped
    public MetricRegistry getBaseRegistry() {
        return get(MetricRegistry.Type.BASE, meterRegistry);
    }

    @Produces
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    @ApplicationScoped
    public MetricRegistry getVendorRegistry() {
        return get(MetricRegistry.Type.VENDOR, meterRegistry);
    }

    public static MetricRegistry get(MetricRegistry.Type type, MeterRegistry meterRegistry) {
        return registries.computeIfAbsent(type, t -> new MetricRegistryImpl(type, meterRegistry));
    }

    static class MetricRegistryImpl extends MetricRegistry {

        final MeterRegistry meterRegistry;
        final io.micrometer.core.instrument.Tag scopeTag;

        MetricRegistryImpl(Type t, MeterRegistry meterRegistry) {
            this.scopeTag = io.micrometer.core.instrument.Tag.of("scope", t.getName());
            this.meterRegistry = meterRegistry;
        }

        @Override
        public <T extends Metric> T register(String s, T t) throws IllegalArgumentException {
            return null;
        }

        @Override
        public <T extends Metric> T register(Metadata metadata, T t) throws IllegalArgumentException {
            return null;
        }

        @Override
        public <T extends Metric> T register(Metadata metadata, T t, Tag... tags) throws IllegalArgumentException {
            return null;
        }

        @Override
        public Counter counter(String s) {
            return null;
        }

        @Override
        public Counter counter(String s, Tag... tags) {
            return null;
        }

        @Override
        public Counter counter(Metadata metadata) {
            return null;
        }

        @Override
        public Counter counter(Metadata metadata, Tag... tags) {
            return null;
        }

        @Override
        public ConcurrentGauge concurrentGauge(String s) {
            return null;
        }

        @Override
        public ConcurrentGauge concurrentGauge(String s, Tag... tags) {
            return null;
        }

        @Override
        public ConcurrentGauge concurrentGauge(Metadata metadata) {
            return null;
        }

        @Override
        public ConcurrentGauge concurrentGauge(Metadata metadata, Tag... tags) {
            return null;
        }

        @Override
        public Histogram histogram(String s) {
            return null;
        }

        @Override
        public Histogram histogram(String s, Tag... tags) {
            return null;
        }

        @Override
        public Histogram histogram(Metadata metadata) {
            return null;
        }

        @Override
        public Histogram histogram(Metadata metadata, Tag... tags) {
            return null;
        }

        @Override
        public Meter meter(String s) {
            return null;
        }

        @Override
        public Meter meter(String s, Tag... tags) {
            return null;
        }

        @Override
        public Meter meter(Metadata metadata) {
            return null;
        }

        @Override
        public Meter meter(Metadata metadata, Tag... tags) {
            return null;
        }

        @Override
        public Timer timer(String s) {
            return null;
        }

        @Override
        public Timer timer(String s, Tag... tags) {
            return null;
        }

        @Override
        public Timer timer(Metadata metadata) {
            return null;
        }

        @Override
        public Timer timer(Metadata metadata, Tag... tags) {
            return null;
        }

        @Override
        public SimpleTimer simpleTimer(String s) {
            return null;
        }

        @Override
        public SimpleTimer simpleTimer(String s, Tag... tags) {
            return null;
        }

        @Override
        public SimpleTimer simpleTimer(Metadata metadata) {
            return null;
        }

        @Override
        public SimpleTimer simpleTimer(Metadata metadata, Tag... tags) {
            return null;
        }

        @Override
        public boolean remove(String s) {
            return false;
        }

        @Override
        public boolean remove(MetricID metricID) {
            return false;
        }

        @Override
        public void removeMatching(MetricFilter metricFilter) {

        }

        @Override
        public SortedSet<String> getNames() {
            return null;
        }

        @Override
        public SortedSet<MetricID> getMetricIDs() {
            return null;
        }

        @Override
        public SortedMap<MetricID, Gauge> getGauges() {
            return null;
        }

        @Override
        public SortedMap<MetricID, Gauge> getGauges(MetricFilter metricFilter) {
            return null;
        }

        @Override
        public SortedMap<MetricID, Counter> getCounters() {
            return null;
        }

        @Override
        public SortedMap<MetricID, Counter> getCounters(MetricFilter metricFilter) {
            return null;
        }

        @Override
        public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges() {
            return null;
        }

        @Override
        public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges(MetricFilter metricFilter) {
            return null;
        }

        @Override
        public SortedMap<MetricID, Histogram> getHistograms() {
            return null;
        }

        @Override
        public SortedMap<MetricID, Histogram> getHistograms(MetricFilter metricFilter) {
            return null;
        }

        @Override
        public SortedMap<MetricID, Meter> getMeters() {
            return null;
        }

        @Override
        public SortedMap<MetricID, Meter> getMeters(MetricFilter metricFilter) {
            return null;
        }

        @Override
        public SortedMap<MetricID, Timer> getTimers() {
            return null;
        }

        @Override
        public SortedMap<MetricID, Timer> getTimers(MetricFilter metricFilter) {
            return null;
        }

        @Override
        public SortedMap<MetricID, SimpleTimer> getSimpleTimers() {
            return null;
        }

        @Override
        public SortedMap<MetricID, SimpleTimer> getSimpleTimers(MetricFilter metricFilter) {
            return null;
        }

        @Override
        public Map<MetricID, Metric> getMetrics() {
            return null;
        }

        @Override
        public Map<String, Metadata> getMetadata() {
            return null;
        }
    }
}
