package dev.ebullient.micrometer.runtime.binder.microprofile;

import javax.annotation.Priority;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.metrics.annotation.Counted;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@SuppressWarnings("unused")
@Counted
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
public class CountedInterceptor {

    // Micrometer meter registry
    final MeterRegistry registry;

    CountedInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }

    @AroundConstruct
    Object countedConstructor(InvocationContext context) throws Exception {
        return increment(context, context.getConstructor().getDeclaringClass().getSimpleName());
    }

    @AroundInvoke
    Object countedMethod(InvocationContext context) throws Exception {
        return increment(context, context.getMethod().getName());
    }

    @AroundTimeout
    Object countedTimeout(InvocationContext context) throws Exception {
        return increment(context, context.getMethod().getName());
    }

    Object increment(InvocationContext context, String methodName) throws Exception {
        Counted item = MicroprofileMetricsBinder.getAnnotation(context, Counted.class);
        if (item != null) {
            Counter.builder(item.name().replace("<method>", methodName))
                    .description(item.description().replace("<method>", methodName))
                    .tags(item.tags())
                    .register(registry)
                    .increment();
        }
        return context.proceed();
    }

}
