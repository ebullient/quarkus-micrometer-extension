package dev.ebullient.micrometer.runtime.binder;

import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class JvmMetricsInfoTest {
    @Test
    public void testJvmInfoMetrics() {
        MeterRegistry registry = new SimpleMeterRegistry();
        new JvmInfoMetrics().bindTo(registry);

        Collection<Gauge> gauges = Search.in(registry).name("jvm.info").gauges();
        Assertions.assertEquals(1, gauges.size(),
                "Should find one jvm.info gauge");

        Gauge jvmInfo = gauges.iterator().next();
        Assertions.assertEquals(1L, jvmInfo.value(),
                "jvm.info gauge should always return 1");

        Meter.Id id = jvmInfo.getId();
        Assertions.assertNotNull(id.getTag("version"),
                "JVM version tag should be defined");
        Assertions.assertNotNull(id.getTag("vendor"),
                "JVM vendor tag should be defined");
        Assertions.assertNotNull(id.getTag("runtime"),
                "JVM runtime tag should be defined");
    }
}
