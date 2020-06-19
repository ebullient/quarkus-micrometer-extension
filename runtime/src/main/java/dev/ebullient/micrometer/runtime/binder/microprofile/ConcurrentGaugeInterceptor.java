package dev.ebullient.micrometer.runtime.binder.microprofile;

import javax.annotation.Priority;
import javax.interceptor.*;

import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;

import io.micrometer.core.instrument.*;

@ConcurrentGauge
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
public class ConcurrentGaugeInterceptor {

    // Micrometer meter registry
    final MeterRegistry registry;

    ConcurrentGaugeInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }

    @AroundConstruct
    Object timedConstructor(InvocationContext context) throws Exception {
        return time(context, context.getConstructor().getDeclaringClass().getSimpleName());
    }

    @AroundInvoke
    Object timedMethod(InvocationContext context) throws Exception {
        return time(context, context.getMethod().getName());
    }

    @AroundTimeout
    Object timedTimeout(InvocationContext context) throws Exception {
        return time(context, context.getMethod().getName());
    }

    Object time(InvocationContext context, String methodName) throws Exception {
        ConcurrentGauge item = MicroprofileMetricsBinder.getAnnotation(context, ConcurrentGauge.class);
        if (item != null) {
            LongTaskTimer.Sample sample = LongTaskTimer
                    .builder(item.name().replace("<method>", methodName))
                    .description(item.description().replace("<method>", methodName))
                    .tags(item.tags())
                    .register(registry)
                    .start();

            try {
                return context.proceed();
            } finally {
                try {
                    sample.stop();
                } catch (Exception e) {
                    // ignoring on purpose
                }
            }
        }
        return context.proceed();
    }
}
