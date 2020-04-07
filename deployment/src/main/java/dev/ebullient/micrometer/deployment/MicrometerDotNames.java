package dev.ebullient.micrometer.deployment;

import org.jboss.jandex.DotName;

public class MicrometerDotNames {

    public static final DotName CLOCK = DotName
            .createSimple("io.micrometer.core.instrument.Clock");

    public static final DotName METER_REGISTRY = DotName
            .createSimple("io.micrometer.core.instrument.MeterRegistry");
    public static final DotName METER_BINDER = DotName
            .createSimple("io.micrometer.core.instrument.binder.MeterBinder");
    public static final DotName METER_FILTER = DotName
            .createSimple("io.micrometer.core.instrument.config.MeterFilter");

    public static final DotName COMPOSITE_METER_REGISTRY = DotName
            .createSimple("io.micrometer.core.instrument.composite.CompositeMeterRegistry");
    public static final DotName PROMETHEUS_REGISTRY = DotName
            .createSimple("io.micrometer.prometheus.PrometheusMeterRegistry");
    public static final DotName PROMETHEUS_CONFIG = DotName
            .createSimple("io.micrometer.prometheus.PrometheusConfig");
    public static final DotName STACKDRIVER_REGISTRY = DotName
            .createSimple("io.micrometer.stackdriver.StackdriverMeterRegistry");
    public static final DotName STACKDRIVER_CONFIG = DotName
            .createSimple("package io.micrometer.stackdriver.StackdriverConfig");

    // public static final DotName METER_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.Meter.class.getName());

    // public static final DotName TIMER_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.Timer.class.getName());

    // public static final DotName COUNTER_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.Counter.class.getName());

    // public static final DotName GAUGE_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.Gauge.class.getName());

    // public static final DotName DISTRIBUTION_SUMMARY_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.DistributionSummary.class.getName());

    // public static final DotName LONG_TASK_TIMER_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.LongTaskTimer.class.getName());

    // public static final DotName FUNCTION_COUNTER_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.FunctionCounter.class.getName());
    // public static final DotName FUNCTION_TIMER_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.FunctionTimer.class.getName());

    // public static final DotName TIME_GAUGE_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.TimeGauge.class.getName());

    // public static final DotName TAG_INTERFACE = DotName
    //         .createSimple(io.micrometer.core.instrument.Tag.class.getName());

    // // annotations
    // public static final DotName COUNTED = DotName.createSimple(io.micrometer.core.annotation.Counted.class.getName());
    // public static final DotName TIMED = DotName.createSimple(io.micrometer.core.annotation.Timed.class.getName());
    // public static final DotName TIMED_SET = DotName.createSimple(io.micrometer.core.annotation.TimedSet.class.getName());
}
