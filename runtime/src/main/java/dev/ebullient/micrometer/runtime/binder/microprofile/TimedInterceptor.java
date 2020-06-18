package dev.ebullient.micrometer.runtime.binder.microprofile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Priority;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.metrics.annotation.Metered;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.arc.ArcInvocationContext;

@SuppressWarnings("unused")
@Metered
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
public class TimedInterceptor {

    // Micrometer meter registry
    final MeterRegistry registry;

    TimedInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }

    @AroundConstruct
    Object timedConstructor(InvocationContext context) throws Exception {
        mark(context, context.getMethod());
        return context.proceed();
    }

    @AroundInvoke
    Object timedMethod(InvocationContext context) throws Exception {
        mark(context, context.getMethod());
        return context.proceed();
    }

    @AroundTimeout
    Object timedTimeout(InvocationContext context) throws Exception {
        mark(context, context.getMethod());
        return context.proceed();
    }

    void mark(InvocationContext context, Method method) {
        String methodName = method == null ? "ctor" : method.getName();
        Set<Annotation> annotations = (Set<Annotation>) context.getContextData()
                .get(ArcInvocationContext.KEY_INTERCEPTOR_BINDINGS);

        for (Annotation a : annotations) {
            if (Metered.class.isInstance(a)) {
                Metered item = (Metered) a;

                Counter.builder(item.name().replace("<method>", methodName))
                        .description(item.description().replace("<method>", methodName))
                        .tags(item.tags())
                        .register(registry)
                        .increment();
                return;
            }
        }
    }
}
