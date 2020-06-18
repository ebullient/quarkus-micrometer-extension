package dev.ebullient.micrometer.runtime.binder.microprofile;

import javax.annotation.Priority;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.metrics.annotation.Timed;

import io.micrometer.core.instrument.MeterRegistry;

@SuppressWarnings("unused")
@Timed
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
        System.out.println("TIMED: " + context);
        //        Timed item = MicroprofileMetricsBinder.getAnnotation(context, Timed.class);
        //        if (item != null) {
        //            Timer.Sample sample = Timer.start(registry);
        //            try {
        //                System.out.println("START TIMED");
        //                return context.proceed();
        //            } finally {
        //                try {
        //                    sample.stop(Timer
        //                            .builder(item.name().replace("<method>", methodName))
        //                            .description(item.description().replace("<method>", methodName))
        //                            .tags(item.tags())
        //                            .register(registry));
        //                } catch (Exception e) {
        //                    // ignoring on purpose
        //                }
        //            }
        //        }
        //        System.out.println("NOT TIMED");
        return context.proceed();
    }
}
