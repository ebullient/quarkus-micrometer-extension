package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeneratedMetricNamesTest {
    @Test
    public void testDotSeparatedNames() {
        Assertions.assertEquals("a.b", MicroprofileMetricsProcessor.dotSeparate("a", "b"));
        Assertions.assertEquals("a.b", MicroprofileMetricsProcessor.dotSeparate("A", "b"));
        Assertions.assertEquals("a.b", MicroprofileMetricsProcessor.dotSeparate("A", "B"));
        Assertions.assertEquals("aa.b", MicroprofileMetricsProcessor.dotSeparate("Aa", "b"));
        Assertions.assertEquals("aa.b", MicroprofileMetricsProcessor.dotSeparate("AaB"));
        Assertions.assertEquals("annotated.gauge.processor",
                MicroprofileMetricsProcessor.dotSeparate("AnnotatedGaugeProcessor"));
    }
}
