package dev.ebullient.micrometer.runtime.binder.microprofile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Priority;
import javax.interceptor.*;

import org.eclipse.microprofile.metrics.annotation.Counted;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.arc.ArcInvocationContext;

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
        increment(context, context.getMethod());
        return context.proceed();
    }

    @AroundInvoke
    Object countedMethod(InvocationContext context) throws Exception {
        increment(context, context.getMethod());
        return context.proceed();
    }

    @AroundTimeout
    Object countedTimeout(InvocationContext context) throws Exception {
        increment(context, context.getMethod());
        return context.proceed();
    }

    void increment(InvocationContext context, Method method) {
        String methodName = method == null ? "ctor" : method.getName();
        Set<Annotation> annotations = (Set<Annotation>) context.getContextData()
                .get(ArcInvocationContext.KEY_INTERCEPTOR_BINDINGS);

        for (Annotation a : annotations) {
            if (Counted.class.isInstance(a)) {
                Counted item = (Counted) a;
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
