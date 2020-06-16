package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricAnnotationInfoTest {
    @Test
    public void testDotSeparatedNames() {
        Assertions.assertEquals("a.b", MetricAnnotationInfo.createMetricName("a", "b"));
        Assertions.assertEquals("a.b", MetricAnnotationInfo.createMetricName("A", "b"));
        Assertions.assertEquals("a.b", MetricAnnotationInfo.createMetricName("A", "B"));
        Assertions.assertEquals("aa.b", MetricAnnotationInfo.createMetricName("Aa", "b"));
        Assertions.assertEquals("aa.b", MetricAnnotationInfo.createMetricName("AaB"));
        Assertions.assertEquals("annotated.gauge.processor",
                MetricAnnotationInfo.createMetricName("AnnotatedGaugeProcessor"));
    }
}
